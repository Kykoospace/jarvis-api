package jarvisapi.controller;

import jarvisapi.entity.User;
import jarvisapi.entity.UserDevice;
import jarvisapi.exception.UserNotFoundException;
import jarvisapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/security")
public class SecurityController {

    @Autowired
    private UserService userService;

    @GetMapping("devices")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity getDevices() {
        try {
            User user = this.userService.getUserFromContext();
            List<UserDevice> userDevices = this.userService.getUserDevices(user);
            return ResponseEntity.status(HttpStatus.OK).body(userDevices);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
