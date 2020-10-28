package jarvisapi.service;

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

import java.util.*;

@Service
public class UserService implements UserDetailsService {

    @Value("${spring.jwt.userPasswordSalt}")
    private String PASSWORD_SALT;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SingleUseTokenService singleUseTokenService;

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
    public User create(
            String firstName,
            String lastName,
            String email
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

    public User activateAccount(String email, String activationToken, String password)
            throws UserNotFoundException, SingleUseTokenNotFoundException, SingleUseTokenExpiredException {

        Optional<User> userOptional = this.userRepository.findByEmail(email);

        if (!userOptional.isPresent()) {
            throw new UserNotFoundException();
        }

        User user = userOptional.get();

        // Token check:
        SingleUseToken singleUseToken = user.getUserSecurity().getAccountValidationToken();
        if (!singleUseToken.getToken().equals(activationToken)) {
            throw new SingleUseTokenNotFoundException();
        }
        if (!this.singleUseTokenService.isSingleUseTokenValid(user.getUserSecurity().getAccountValidationToken().getId())) {
            throw new SingleUseTokenExpiredException();
        }

        // Activate account:
        user.getUserSecurity().setAccountValidationToken(null);
        user.getUserSecurity().setAccountEnabled(true);
        user.getUserSecurity().setPassword(this.encodePassword(password));

        return this.userRepository.save(user);
    }

    /**
     * Set a new activation single use token
     * @param id
     * @throws UserNotFoundException
     */
    public void setNewActivationToken(long id) throws UserNotFoundException {
        Optional<User> userOptional = this.userRepository.findById(id);

        if (!userOptional.isPresent()) {
            throw new UserNotFoundException();
        }

        User user = userOptional.get();
        SingleUseToken singleUseToken = this.singleUseTokenService.create();
        user.getUserSecurity().setAccountValidationToken(singleUseToken);

        this.userRepository.save(user);
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
     * @param type
     * @return
     */
    public UserDevice createUserDevice(String macAddress, String type) {
        UserDevice userDevice = new UserDevice(macAddress, type);

        return this.userDeviceRepository.save(userDevice);
    }

    /**
     * Create first user device
     * @param macAddress
     * @param type
     * @return
     */
    public UserDevice createFirstUserDevice(String macAddress, String type) {
        UserDevice userDevice = new UserDevice(macAddress, type);
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
