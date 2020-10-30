package jarvisapi.service;

import freemarker.template.TemplateException;
import jarvisapi.entity.*;
import jarvisapi.exception.*;
import jarvisapi.repository.UserDeviceRepository;
import jarvisapi.repository.UserRepository;
import jarvisapi.repository.UserSecurityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.*;

@Service
public class UserService implements UserDetailsService {

    @Value("${spring.jwt.userPasswordSalt}")
    private String PASSWORD_SALT;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSecurityRepository userSecurityRepository;

    @Autowired
    private SingleUseTokenService singleUseTokenService;

    @Autowired
    private MailerService mailerService;

    @Autowired
    private UserDeviceRepository userDeviceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User getUserFromContext() throws UserNotFoundException {
        org.springframework.security.core.userdetails.User userDetails = (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> user = this.userRepository.findByEmail(userDetails.getUsername());

        if (!user.isPresent()) {
            throw new UserNotFoundException();
        }

        return user.get();
    }

    /**
     * Get all User
     * @return
     */
    public List<User> getAll() {
        return this.userRepository.findAll();
    }

    /**
     * Get User
     * @param id
     * @return
     */
    public User get(long id) throws UserNotFoundException {
        Optional<User> user = this.userRepository.findById(id);

        if (!user.isPresent()) {
            throw new UserNotFoundException();
        }

        return user.get();
    }

    /**
     * Get User by email
     * @param username
     * @return
     */
    public User getByUsername(String username) throws UserNotFoundException {
        Optional<User> user = this.userRepository.findByEmail(username);

        if (!user.isPresent()) {
            throw new UserNotFoundException();
        }

        return user.get();
    }

    /**
     * Check if email is available
     * @param email
     * @return
     */
    public boolean isEmailAvailable(String email) {
        Optional<User> user = this.userRepository.findByEmail(email);
        return !user.isPresent();
    }

    /**
     * Create User
     * @param firstName
     * @param lastName
     * @param email
     * @return
     */
    public User create(String firstName, String lastName, String email
    ) {
        // User security config with random password :
        String randomPassword = UUID.randomUUID().toString();
        UserSecurity userSecurity = new UserSecurity(this.encodePassword(randomPassword));

        // User creation :
        User user = new User(firstName, lastName, email, userSecurity);

        // Add default task collection :
        TaskCollection taskCollection = new TaskCollection("Mes tâches", false);
        user.setTaskCollections(Collections.singletonList(taskCollection));

        return this.userRepository.save(user);
    }

    /**
     * Update User
     * @param id
     * @param firstName
     * @param lastName
     * @param email
     * @return
     * @throws UserNotFoundException
     */
    public User update(long id, String firstName, String lastName, String email) throws UserNotFoundException {
        Optional<User> user = this.userRepository.findById(id);

        if (!user.isPresent()) {
            throw new UserNotFoundException();
        }

        // Update fields :
        user.get().setFirstName(firstName);
        user.get().setLastName(lastName);
        user.get().setEmail(email);

        return this.userRepository.save(user.get());
    }

    /**
     * Delete User
     * @param id
     * @throws UserNotFoundException
     */
    public void delete(long id) throws UserNotFoundException {
        Optional<User> user = this.userRepository.findById(id);

        if (!user.isPresent()) {
            throw new UserNotFoundException();
        }

        this.userRepository.delete(user.get());
    }

    /**
     * Get User authentication
     * @param email
     * @param password
     * @return
     */
    public UsernamePasswordAuthenticationToken getUserAuthentication(String email, String password) {
        return new UsernamePasswordAuthenticationToken(email, this.saltPassword(password));
    }

    public User activateAccount(String email, String accountActivationToken, String password, String macAddress, String publicIp, String deviceType)
            throws UserNotFoundException, SingleUseTokenNotFoundException, SingleUseTokenExpiredException {
        Optional<User> userOptional = this.userRepository.findByEmail(email);

        if (!userOptional.isPresent()) {
            throw new UserNotFoundException();
        }

        User user = userOptional.get();

        // Token check:
        if (!this.singleUseTokenService.isSingleUseTokenVerified(user.getUserSecurity().getAccountValidationToken(), accountActivationToken)) {
            throw new SingleUseTokenNotFoundException();
        }

        // Activate account:
        this.singleUseTokenService.delete(user.getUserSecurity().getAccountValidationToken().getId());
        user.getUserSecurity().setAccountEnabled(true);
        user.getUserSecurity().setPassword(this.encodePassword(password));

        // Set new authorized device:
        this.createFirstUserDevice(macAddress, publicIp, deviceType);

        return this.userRepository.save(user);
    }

    public boolean checkAccountActivationTokenValidity(String email, String accountActivationToken)
            throws UserNotFoundException, SingleUseTokenNotFoundException {
        Optional<User> userOptional = this.userRepository.findByEmail(email);

        if (!userOptional.isPresent()) {
            throw new UserNotFoundException();
        }

        User user = userOptional.get();

        // Token check
        SingleUseToken singleUseToken = user.getUserSecurity().getAccountValidationToken();
        if (!singleUseToken.getToken().toString().equals(accountActivationToken)) {
            throw new SingleUseTokenNotFoundException();
        }

        return this.singleUseTokenService.isSingleUseTokenValid(singleUseToken);
    }

    /**
     * Set a new activation single use token
     * @param userEmail
     * @throws UserNotFoundException
     */
    public void setNewActivationToken(String userEmail)
            throws UserNotFoundException, MessagingException, IOException, TemplateException {
        Optional<User> userOptional = this.userRepository.findByEmail(userEmail);

        if (!userOptional.isPresent()) {
            throw new UserNotFoundException();
        }

        User user = userOptional.get();
        UserSecurity userSecurity = user.getUserSecurity();

        // Set a new account activation token:
        SingleUseToken singleUseToken = this.singleUseTokenService.create();
        long lastTokenId = userSecurity.getAccountValidationToken().getId();
        userSecurity.setAccountValidationToken(singleUseToken);

        this.userSecurityRepository.save(userSecurity);

        // Remove the last token
        try {
            this.singleUseTokenService.delete(lastTokenId);
        } catch (Exception e) { }

        // Send the account activation email:
        this.mailerService.sendAccountActivationMail(user.getEmail(), singleUseToken.getToken().toString());
    }

    /**
     * Change User password
     * @param id
     * @param password
     * @return
     * @throws UserSecurityNotFoundException
     */
    public User changePassword(long id, String password) throws UserSecurityNotFoundException {
        Optional<User> user = this.userRepository.findById(id);

        if (!user.isPresent()) {
            throw new UserNotFoundException();
        }

        String encodedPassword = this.encodePassword(password);
        user.get().getUserSecurity().setPassword(encodedPassword);

        return this.userRepository.save(user.get());
    }

    /**
     * Get user devices
     * @return
     */
    public List<UserDevice> getUserDevices() {
        return userDeviceRepository.findAll();
    }

    /**
     * Get user device
     * @param id
     * @return
     */
    public UserDevice getUserDevice(long id) {
        Optional<UserDevice> userDevice = this.userDeviceRepository.findById(id);

        if (!userDevice.isPresent()) {
            throw new UserDeviceNotFoundException();
        }

        return userDevice.get();
    }

    /**
     * Create user device
     * @param macAddress
     * @param publicIp
     * @param deviceType
     * @return
     */
    public UserDevice createUserDevice(String macAddress, String publicIp, String deviceType) {
        UserDevice userDevice = new UserDevice(macAddress, publicIp, deviceType);

        return this.userDeviceRepository.save(userDevice);
    }

    /**
     * Create first user device
     * @param macAddress
     * @param publicIp
     * @param deviceType
     * @return
     */
    public UserDevice createFirstUserDevice(String macAddress, String publicIp, String deviceType) {
        UserDevice userDevice = new UserDevice(macAddress, publicIp, deviceType);
        userDevice.setAuthorized(true);

        return this.userDeviceRepository.save(userDevice);
    }

    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        Optional<User> user = this.userRepository.findByEmail(userEmail);

        if (!user.isPresent()) {
            throw new UsernameNotFoundException("Invalid username or password.");
        }

        return new org.springframework.security.core.userdetails.User(
                user.get().getEmail(),
                user.get().getUserSecurity().getPassword(),
                this.getAuthority(user.get())
        );
    }

    /**
     * Get encoded salted password
     * @param password
     * @return
     */
    private String encodePassword(String password) {
        return this.passwordEncoder.encode(this.saltPassword(password));
    }

    /**
     * Salt password
     * @param password
     * @return
     */
    private String saltPassword(String password) {
        return password + this.PASSWORD_SALT;
    }

    /**
     * Get user authorities
     * @param user
     * @return
     */
    private Set<SimpleGrantedAuthority> getAuthority(User user) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(user.getUserSecurity().isAdmin() ? "ADMIN" : "USER"));

        return authorities;
    }
}
