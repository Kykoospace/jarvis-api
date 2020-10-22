package jarvisapi.controller;

import jarvisapi.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class TestController {


    @GetMapping("")
    public ResponseEntity helloWorld() {
        return ResponseEntity.status(HttpStatus.OK).body("Hello world!");
    }

    @GetMapping("/protected")
    public ResponseEntity protectedArea() {
        return ResponseEntity.status(HttpStatus.OK).body("This is a protected area");
    }
}
