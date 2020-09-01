package jarvisapi.service;

import jarvisapi.entity.TaskCollection;
import jarvisapi.entity.User;
import jarvisapi.entity.UserSecurity;
import jarvisapi.exception.UserNotFoundException;
import jarvisapi.exception.UserSecurityNotFoundException;
import jarvisapi.repository.UserRepository;
import jarvisapi.repository.UserSecurityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSecurityRepository userSecurityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    public User get(Long id) {
        Optional<User> user = this.userRepository.findById(id);

        if (!user.isPresent()) {
            throw new UserNotFoundException();
        }

        return user.get();
    }

    /**
     * Create User
     * @param firstName
     * @param lastName
     * @param email
     * @param password
     * @return
     */
    public User create(
            String firstName,
            String lastName,
            String email,
            String password
    ) {
        // User security config :
        String encodedPassword = this.passwordEncoder.encode(password);
        UserSecurity userSecurity = new UserSecurity(encodedPassword);

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
    public User update(Long id, String firstName, String lastName, String email) throws UserNotFoundException {
        Optional<User> user = this.userRepository.findById(id);

        if (!user.isPresent()) {
            throw new UserNotFoundException();
        }

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
    public void delete(Long id) throws UserNotFoundException {
        Optional<User> user = this.userRepository.findById(id);

        if (!user.isPresent()) {
            throw new UserNotFoundException();
        }

        this.userRepository.delete(user.get());
    }

    /**
     * Change User password
     * @param id
     * @param password
     * @return
     * @throws UserSecurityNotFoundException
     */
    public UserSecurity changePassword(Long id, String password) throws UserSecurityNotFoundException {
        Optional<UserSecurity> userSecurity = this.userSecurityRepository.findById(id);

        if (!userSecurity.isPresent()) {
            throw new UserSecurityNotFoundException();
        }

        String encodedPassword = this.passwordEncoder.encode(password);
        userSecurity.get().setPassword(encodedPassword);

        return this.userSecurityRepository.save(userSecurity.get());
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

    private Set getAuthority(User user) {
        Set authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(user.getUserSecurity().isAdmin() ? "ADMIN" : "USER"));

        return authorities;
    }
}
