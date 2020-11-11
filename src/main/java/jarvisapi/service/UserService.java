package jarvisapi.service;

import freemarker.template.TemplateException;
import jarvisapi.dto.UserDTO;
import jarvisapi.dto.UserDeviceDTO;
import jarvisapi.entity.*;
import jarvisapi.exception.*;
import jarvisapi.mapper.UserDeviceMapper;
import jarvisapi.mapper.UserMapper;
import jarvisapi.repository.DeviceConnectionRepository;
import jarvisapi.repository.UserDeviceRepository;
import jarvisapi.repository.UserRepository;
import jarvisapi.repository.UserSecurityRepository;
import jarvisapi.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    @Value("${spring.jwt.userPasswordSalt}")
    private String PASSWORD_SALT;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserDeviceMapper userDeviceMapper;

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
    private DeviceConnectionRepository deviceConnectionRepository;

    @Autowired
    private FolderService folderService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Get user from context
     * @return
     * @throws UserNotFoundException
     */
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
    public List<UserDTO> getAll() {
        List<User> users = this.userRepository.findAll();
        return this.userMapper.toUserDTO(users);
    }

    /**
     * Get User
     * @param id
     * @return
     */
    public UserDTO get(long id) throws UserNotFoundException {
        Optional<User> user = this.userRepository.findById(id);

        if (!user.isPresent()) {
            throw new UserNotFoundException();
        }

        return this.userMapper.toUserDTO(user.get());
    }

    /**
     * Get User by email
     * @param username
     * @return
     */
    public UserDTO getByUsername(String username) throws UserNotFoundException {
        Optional<User> userOpt = this.userRepository.findByEmail(username);

        if (!userOpt.isPresent()) {
            throw new UserNotFoundException();
        }

        User user = userOpt.get();

        return this.userMapper.toUserDTO(user);
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
        this.userRepository.save(user);

        // Set the home folder:
        user.setHomeFolder(this.folderService.createHomeFolder(user));

        return this.userRepository.save(user);
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
     * Check if email is available
     * @param email
     * @return
     */
    public boolean isEmailAvailable(String email) {
        Optional<User> user = this.userRepository.findByEmail(email);
        return !user.isPresent();
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

    /**
     * Activate account
     * @param userEmail
     * @param accountActivationToken
     * @param password
     * @param publicIp
     * @param deviceType
     * @return
     * @throws UserNotFoundException
     * @throws SingleUseTokenNotFoundException
     * @throws SingleUseTokenExpiredException
     */
    public User activateAccount(String userEmail, String accountActivationToken, String password, String publicIp, String deviceType)
            throws UserNotFoundException, SingleUseTokenNotFoundException, SingleUseTokenExpiredException {
        Optional<User> userOptional = this.userRepository.findByEmail(userEmail);

        if (!userOptional.isPresent()) {
            throw new UserNotFoundException();
        }

        User user = userOptional.get();
        UserSecurity userSecurity = user.getUserSecurity();

        // Token check:
        if (!this.singleUseTokenService.isSingleUseTokenVerified(user.getUserSecurity().getAccountValidationToken(), accountActivationToken)) {
            throw new SingleUseTokenNotFoundException();
        }

        // Activate account:
        long lastTokenId = userSecurity.getAccountValidationToken().getId();
        userSecurity.setAccountEnabled(true);
        userSecurity.setPassword(this.encodePassword(password));
        userSecurity.setAccountValidationToken(null);
        this.userSecurityRepository.save(userSecurity);
        this.singleUseTokenService.delete(lastTokenId);

        // Set new authorized device:
        this.createFirstUserDevice(user.getUserSecurity(), publicIp, deviceType);

        return this.userRepository.save(user);
    }

    /**
     * Check account activation token validity
     * @param userEmail
     * @param accountActivationToken
     * @return
     * @throws UserNotFoundException
     * @throws SingleUseTokenNotFoundException
     */
    public boolean checkAccountActivationTokenValidity(String userEmail, String accountActivationToken)
            throws UserNotFoundException, UserAccountEnabledException, SingleUseTokenNotFoundException {
        Optional<User> userOptional = this.userRepository.findByEmail(userEmail);

        if (!userOptional.isPresent()) {
            throw new UserNotFoundException();
        }

        User user = userOptional.get();
        if (user.getUserSecurity().isAccountEnabled()) {
            throw new UserAccountEnabledException();
        }

        // Token check:
        SingleUseToken singleUseToken = user.getUserSecurity().getAccountValidationToken();
        if (!singleUseToken.getToken().toString().equals(accountActivationToken)) {
            throw new SingleUseTokenNotFoundException();
        }

        return this.singleUseTokenService.isSingleUseTokenValid(singleUseToken);
    }

    /**
     * Request a new account activation token and send email
     * @param userEmail
     * @throws MessagingException
     * @throws IOException
     * @throws TemplateException
     */
    public void requestNewActivationToken(String userEmail) throws UserNotFoundException, UserAccountEnabledException, MessagingException, IOException, TemplateException {
        Optional<User> userOpt = this.userRepository.findByEmail(userEmail);

        if (!userOpt.isPresent()) {
            throw new UserNotFoundException();
        }

        User user = userOpt.get();
        if (user.getUserSecurity().isAccountEnabled()) {
            throw new UserAccountEnabledException();
        }

        this.setActivationToken(user);
        this.sendAccountActivationEmail(user);
    }

    /**
     * Set a new activation single use token and send email
     * @param user
     * @throws UserNotFoundException
     */
    public void setActivationToken(User user)
            throws UserNotFoundException, MessagingException, IOException, TemplateException {
        UserSecurity userSecurity = user.getUserSecurity();

        // Set a new account activation token:
        SingleUseToken singleUseToken = this.singleUseTokenService.create();
        try { // Remove the last token
            long lastTokenId = userSecurity.getAccountValidationToken().getId();
            this.singleUseTokenService.delete(lastTokenId);
        } catch (Exception e) { }
        userSecurity.setAccountValidationToken(singleUseToken);

        this.userSecurityRepository.save(userSecurity);
    }

    /**
     * Send account activation email
     * @param user
     * @throws MessagingException
     * @throws IOException
     * @throws TemplateException
     */
    public void sendAccountActivationEmail(User user) throws MessagingException, IOException, TemplateException {
        this.mailerService.sendAccountActivationMail(
                user.getFirstName(),
                user.getEmail(),
                user.getUserSecurity().getAccountValidationToken().getToken().toString());
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
     * Get user devices of context user
     * @return
     */
    public List<UserDeviceDTO> getUserDevices(long userId) {
        User user = this.userRepository.getOne(userId);
        List<UserDevice> userDevices = user.getUserSecurity().getDevices();
        return this.userDeviceMapper.toUserDTO(userDevices);
    }

    /**
     * Create user device
     * @param publicIp
     * @param deviceType
     * @return
     */
    public UserDevice createUserDevice(UserSecurity userSecurity, String publicIp, String deviceType) {
        UserDevice userDevice = new UserDevice(publicIp, deviceType);
        userDevice.setUserSecurity(userSecurity);
        userDevice.setVerificationToken(this.singleUseTokenService.create());

        return this.userDeviceRepository.save(userDevice);
    }

    /**
     * Create first user device
     * @param publicIp
     * @param deviceType
     * @return
     */
    public UserDevice createFirstUserDevice(UserSecurity userSecurity, String publicIp, String deviceType) {
        UserDevice userDevice = new UserDevice(publicIp, deviceType);
        userDevice.setUserSecurity(userSecurity);
        userDevice.setVerified(true);
        userDevice.setVerificationDate(new Date());

        return this.userDeviceRepository.save(userDevice);
    }

    /**
     * Check user device
     * @param userEmail
     * @param publicIp
     * @return
     * @throws UserDeviceNotFoundException
     * @throws MessagingException
     * @throws IOException
     * @throws TemplateException
     */
    public UserDevice checkUserDevice(String userEmail, String publicIp) throws UserDeviceNotFoundException, MessagingException, IOException, TemplateException {
        Optional<User> userOpt = this.userRepository.findByEmail(userEmail);
        Optional<UserDevice> userDeviceOpt = this.userDeviceRepository.getByUserEmailAndPublicIp(userEmail, publicIp);

        if (!userOpt.isPresent()) {
            throw new UserNotFoundException();
        }
        if (!userDeviceOpt.isPresent()) {
            throw new UserDeviceNotFoundException();
        }

        User user = userOpt.get();
        UserDevice userDevice = userDeviceOpt.get();

        if (userDevice.isVerified()) {
            return userDevice;
        }

        // Set userDevice a new token:
        SingleUseToken oldSingleUseToken = userDevice.getVerificationToken();
        userDevice.setVerificationToken(null);
        this.singleUseTokenService.delete(oldSingleUseToken.getId());
        userDevice.setVerificationToken(this.singleUseTokenService.create());
        this.userDeviceRepository.save(userDevice);

        // Send device activation email:
        this.mailerService.sendTrustDeviceVerificationMail(
                user.getFirstName(),
                user.getEmail(),
                userDevice.getVerificationToken().getToken().toString());

        throw new UserDeviceNotAuthorizedException();
    }

    /**
     * Register connection
     * @param userEmail
     * @param publicIp
     * @param deviceType
     * @param browser
     * @return
     */
    public DeviceConnection registerConnexion(String userEmail, String publicIp, String deviceType, String browser) {
        Optional<UserDevice> userDeviceOpt = this.userDeviceRepository.getByUserEmailAndPublicIp(userEmail, publicIp);

        if (!userDeviceOpt.isPresent()) {
            Optional<User> userOpt = this.userRepository.findByEmail(userEmail);

            if (!userOpt.isPresent() || !userOpt.get().getUserSecurity().isAccountEnabled()) {
                throw new UserNotFoundException();
            }

            UserDevice newUserDevice = this.createUserDevice(userOpt.get().getUserSecurity(), publicIp, deviceType);
            userDeviceOpt = this.userDeviceRepository.findById(newUserDevice.getId());
        }

        DeviceConnection deviceConnection = new DeviceConnection(userDeviceOpt.get(), browser);
        return this.deviceConnectionRepository.save(deviceConnection);
    }

    /**
     * Set device credentialConnection successful
     * @param deviceConnection
     */
    public void setDeviceCredentialConnectionSuccessful(DeviceConnection deviceConnection) {
        deviceConnection.setCredentialSuccess(true);
        this.deviceConnectionRepository.save(deviceConnection);
    }

    /**
     * Set device connection successful
     * @param deviceConnection
     */
    public void setDeviceConnectionSuccessful(DeviceConnection deviceConnection) {
        deviceConnection.setSuccess(true);
        this.deviceConnectionRepository.save(deviceConnection);
    }

    /**
     * Verify device
     * @param userEmail
     * @param deviceActivationToken
     * @throws UserNotFoundException
     * @throws UserDeviceNotFoundException
     * @throws SingleUseTokenExpiredException
     */
    public void verifyDevice(String userEmail, String deviceActivationToken)
            throws UserNotFoundException, UserDeviceNotFoundException, SingleUseTokenExpiredException {
        Optional<User> userOptional = this.userRepository.findByEmail(userEmail);

        if (!userOptional.isPresent()) {
            throw new UserNotFoundException();
        }

        User user = userOptional.get();

        // Check token's value by filtering devices:
        UserDevice userDevice = user.getUserSecurity().getDevices()
                .stream()
                // Filter by token value:
                .filter(device -> !device.isVerified())
                .filter(device -> deviceActivationToken.equals(device.getVerificationToken().getToken().toString()))
                .findAny()
                .orElse(null);

        if (userDevice == null) {
            throw new UserDeviceNotFoundException();
        }

        // Check token's expiration date:
        if (DateUtils.isDateExpired(userDevice.getVerificationToken().getExpirationDate())) {
            throw new SingleUseTokenExpiredException();
        }

        // Set device verified:
        long lastTokenId = userDevice.getVerificationToken().getId();
        userDevice.setVerified(true);
        userDevice.setVerificationDate(new Date());
        userDevice.setVerificationToken(null);
        this.singleUseTokenService.delete(lastTokenId);
        this.userDeviceRepository.save(userDevice);
    }

    /**
     * Check device verification token validity
     * @param userEmail
     * @param deviceActivationToken
     * @return
     * @throws UserNotFoundException
     * @throws UserDeviceNotFoundException
     */
    public boolean checkDeviceVerificationTokenValidity(String userEmail, String deviceActivationToken)
            throws UserNotFoundException, UserDeviceNotFoundException {
        Optional<User> userOptional = this.userRepository.findByEmail(userEmail);

        if (!userOptional.isPresent()) {
            throw new UserNotFoundException();
        }

        User user = userOptional.get();

        // Check token's value by filtering devices:
        UserDevice userDevice = user.getUserSecurity().getDevices()
                .stream()
                // Filter by token value:
                .filter(device -> !device.isVerified())
                .filter(device -> deviceActivationToken.equals(device.getVerificationToken().getToken().toString()))
                .findAny()
                .orElse(null);

        if (userDevice == null) {
            throw new UserDeviceNotFoundException();
        }

        return this.singleUseTokenService.isSingleUseTokenValid(userDevice.getVerificationToken());
    }

    /**
     * Load user by username
     * @param userEmail
     * @return
     * @throws UsernameNotFoundException
     */
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
