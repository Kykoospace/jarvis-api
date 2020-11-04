package jarvisapi.controller;

import jarvisapi.dto.UserDTO;
import jarvisapi.dto.UserDeviceDTO;
import jarvisapi.exception.UserNotFoundException;
import jarvisapi.mapper.UserMapper;
import jarvisapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("user")
    public ResponseEntity getUser() {
        try {
            UserDTO userDTO = this.userService.getUserFromContext();
            return ResponseEntity.status(HttpStatus.OK).body(userDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("users")
    public ResponseEntity getUsers() {
        try {
            List<UserDTO> users = this.userService.getAll();
            return ResponseEntity.status(HttpStatus.OK).body(users);
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("users/{id}")
    public ResponseEntity getUser(@PathVariable long id) {
        try {
            UserDTO userDTO = this.userService.get(id);
            return ResponseEntity.status(HttpStatus.OK).body(userDTO);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
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

    @GetMapping("devices")
    public ResponseEntity getDevices() {
        try {
            UserDTO userDTO = this.userService.getUserFromContext();
            List<UserDeviceDTO> userDevices = this.userService.getUserDevices(userDTO.getId());
            return ResponseEntity.status(HttpStatus.OK).body(userDevices);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
