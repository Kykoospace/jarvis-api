package jarvisapi.controller;

import jarvisapi.entity.SignUpRequest;
import jarvisapi.entity.SingleUseToken;
import jarvisapi.entity.User;
import jarvisapi.exception.SignUpRequestNotFoundException;
import jarvisapi.exception.SingleUseTokenExpiredException;
import jarvisapi.exception.SingleUseTokenNotFoundException;
import jarvisapi.exception.UserNotFoundException;
import jarvisapi.payload.request.*;
import jarvisapi.payload.response.SignInResponse;
import jarvisapi.utils.DateUtils;
import jarvisapi.utils.JwtTokenUtil;
import jarvisapi.service.SignUpRequestService;
import jarvisapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private SignUpRequestService signUpRequestService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity signUp(@Valid @RequestBody jarvisapi.payload.request.SignUpRequest signUp) {
        try {
            this.signUpRequestService.create(signUp.getFirstName(), signUp.getLastName(), signUp.getEmail());
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/email-validity")
    public ResponseEntity checkEmailValidity(@Valid @RequestBody EmailValidityRequest emailValidityRequest) {
        if (emailValidityRequest.getEmail() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        final boolean emailAvailableForUsers = this.userService.isEmailAvailable(emailValidityRequest.getEmail());
        final boolean emailAvailableForSignUpRequests = this.signUpRequestService.isEmailAvailable(emailValidityRequest.getEmail());

        return ResponseEntity.status(HttpStatus.OK).body(emailAvailableForUsers && emailAvailableForSignUpRequests);
    }

    @PostMapping("/sign-in")
    public ResponseEntity signIn(@Valid @RequestBody SignInRequest signInRequest) {
        /*
         * TODO: User connection
         * Verify user password
         * If the connection is authorized :
         *  Check authorized devices
         *  If this device is authorized :
         *   Save the new connection history
         *   Return JWT
         *  Else
         *   Return forbidden access
         * Else
         *  Return forbidden access
         */
        final Authentication authentication = authenticationManager.authenticate(
                this.userService.getUserAuthentication(signInRequest.getEmail(), signInRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = this.userService.getByUsername(authentication.getName());

        return ResponseEntity.ok(
                new SignInResponse(
                        jwtTokenUtil.generateToken(authentication),
                        "",
                        user.getUserSecurity().isAdmin(),
                        user
                )
        );
    }

    @GetMapping("/sign-up-request")
    public ResponseEntity getSignUpRequests() {
        try {
            List<SignUpRequest> signUpRequests = this.signUpRequestService.getAll();
            return ResponseEntity.status(HttpStatus.OK).body(signUpRequests);
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/sign-up-request/{id}")
    public ResponseEntity getSignUpRequest(@PathVariable long id) {
        try {
            SignUpRequest signUpRequest = this.signUpRequestService.get(id);
            return ResponseEntity.status(HttpStatus.OK).body(signUpRequest);
        } catch(SignUpRequestNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/sign-up-request/{id}/accept")
    public ResponseEntity acceptSignUpRequest(@PathVariable long id) {
        try {
            this.signUpRequestService.accept(id);
            List<SignUpRequest> signUpRequests = this.signUpRequestService.getAll();
            return ResponseEntity.status(HttpStatus.OK).body(signUpRequests);
        } catch (SignUpRequestNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/sign-up-request/{id}/reject")
    public ResponseEntity rejectSignUpRequest(@PathVariable long id) {
        try {
            this.signUpRequestService.reject(id);
            List<SignUpRequest> signUpRequests = this.signUpRequestService.getAll();
            return ResponseEntity.status(HttpStatus.OK).body(signUpRequests);
        } catch (SignUpRequestNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/activate-account")
    public ResponseEntity accountActivation(@Valid @RequestBody AccountActivationRequest accountActivationRequest) {
        try {
            this.userService.activateAccount(
                    accountActivationRequest.getEmail(),
                    accountActivationRequest.getToken(),
                    accountActivationRequest.getPassword(),
                    accountActivationRequest.getPublicIp(),
                    accountActivationRequest.getDeviceType());

            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (SingleUseTokenNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (SingleUseTokenExpiredException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/activate-account/check-token")
    public ResponseEntity accountActivationCheckTokenValidity(@RequestBody AccountActivationTokenValidityRequest accountActivationTokenValidityRequest) {
        try {
            boolean isTokenValid = this.userService.checkAccountActivationTokenValidity(
                    accountActivationTokenValidityRequest.getEmail(),
                    accountActivationTokenValidityRequest.getToken());

            return ResponseEntity.status(HttpStatus.OK).body(isTokenValid);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (SingleUseTokenNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/activate-account/new-token")
    public ResponseEntity newAccountActivationToken(@RequestBody NewAccountActivationTokenRequest newAccountActivationTokenRequest) {
        try {
            if (!this.userService.getByUsername(newAccountActivationTokenRequest.getEmail()).getUserSecurity().isAccountEnabled()) {
                this.userService.setNewActivationToken(newAccountActivationTokenRequest.getEmail());
                return ResponseEntity.status(HttpStatus.OK).body(null);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
