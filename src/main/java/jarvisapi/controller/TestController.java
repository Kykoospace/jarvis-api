package jarvisapi.controller;

import jarvisapi.entity.User;
import jarvisapi.service.MailerService;
import jarvisapi.service.SingleUseTokenService;
import jarvisapi.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @Autowired
    private SingleUseTokenService singleUseTokenService;

    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping("/hello-world")
    public ResponseEntity helloWorld() {
        return ResponseEntity.status(HttpStatus.OK).body("Hello world!");
    }

    @GetMapping("/protected")
    public ResponseEntity protectedArea() {
        return ResponseEntity.status(HttpStatus.OK).body("This is a protected area");
    }

    @GetMapping("/show-info")
    public ResponseEntity deleteToken(HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(securityUtils.getRemoteInfos(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
