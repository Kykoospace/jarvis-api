package jarvisapi.controller;

import jarvisapi.entity.User;
import jarvisapi.payload.request.LoginRequest;
import jarvisapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostMapping("/auth/sign-up")
    public void signUp(@Valid @RequestBody User user) {
        user.setPassword(this.bCryptPasswordEncoder.encode(user.getPassword()));
        this.userRepository.save(user);
    }
}
