package jarvisapi.controller;

import jarvisapi.entity.User;
import jarvisapi.repository.UserRepository;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUsersById(@PathVariable(value = "id") Long userId) throws ResourceNotFoundException {
        User user = this.userRepository
                .findById(userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found on :: " + userId)
                );
        return ResponseEntity.ok().body(user);
    }

    @PostMapping("/users")
    public User createUser(@Valid @RequestBody User user) {
        return this.userRepository.save(user);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable(value = "id") Long userId, @Valid @RequestBody User userDetails) throws ResourceNotFoundException {
        // Get the user :
        User user = this.userRepository
                .findById(userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found on :: " + userId)
                );

        // Update user fields :
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setEmail(userDetails.getEmail());

        final User updatedUser = this.userRepository.save(user);
        return ResponseEntity.ok().body(updatedUser);
    }

    @DeleteMapping("/users/{id}")
    public Map<String, Boolean> deleteUser(@PathVariable(value = "id") Long userId) throws ResourceNotFoundException {
        // Get the user :
        User user = this.userRepository
                .findById(userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found on :: " + userId)
                );

        // Delete the user :
        this.userRepository.delete(user);

        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return response;
    }
}
