package jarvisapi.controller;

import jarvisapi.entity.User;
import jarvisapi.exception.UserNotFoundException;
import jarvisapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("users")
    public ResponseEntity getUsers() {
        try {
            List<User> users = this.userService.getAll();
            return ResponseEntity.status(HttpStatus.OK).body(users);
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("users/{id}")
    public ResponseEntity getUser(@PathVariable long id) {
        try {
            User user = this.userService.get(id);
            return ResponseEntity.status(HttpStatus.OK).body(user);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("users/{id}")
    public ResponseEntity deleteUser(@PathVariable long id) {
        try {
            this.userService.delete(id);
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (UserNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
