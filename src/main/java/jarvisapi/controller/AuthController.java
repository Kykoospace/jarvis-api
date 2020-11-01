package jarvisapi.controller;

import freemarker.template.TemplateException;
import jarvisapi.entity.DeviceConnection;
import jarvisapi.entity.SignUpRequest;
import jarvisapi.entity.User;
import jarvisapi.entity.UserDevice;
import jarvisapi.exception.*;
import jarvisapi.payload.request.*;
import jarvisapi.payload.response.SignInResponse;
import jarvisapi.utils.JwtTokenUtil;
import jarvisapi.service.SignUpRequestService;
import jarvisapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

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
    public ResponseEntity signIn(@Valid @RequestBody SignInRequest signInRequest, HttpServletRequest httpServletRequest) {
        try {
            // New Connexion registration:
            DeviceConnection deviceConnection = this.userService.registerConnexion(signInRequest.getEmail(), httpServletRequest.getRemoteAddr(), signInRequest.getDeviceType(), signInRequest.getBrowser());

            // Authentication validation:
            final Authentication authentication = authenticationManager.authenticate(
                    this.userService.getUserAuthentication(signInRequest.getEmail(), signInRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Updating connection success if authentication is validated:
            this.userService.setDeviceConnectionSuccessful(deviceConnection);

            // User trust device validation:
            User user = this.userService.getByUsername(authentication.getName());
            this.userService.checkUserDevice(user, httpServletRequest.getRemoteAddr());

            SignInResponse signInResponse = new SignInResponse(
                    jwtTokenUtil.generateToken(authentication),
                    "",
                    user.getUserSecurity().isAdmin(),
                    user);

            return ResponseEntity.status(HttpStatus.OK).body(signInResponse);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (UserDeviceNotAuthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("messaging exception");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("io exception");
        } catch (TemplateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("template exception");
        }
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
    public ResponseEntity accountActivation(@Valid @RequestBody AccountActivationRequest accountActivationRequest, HttpServletRequest request) {
        try {
            this.userService.activateAccount(
                    accountActivationRequest.getEmail(),
                    accountActivationRequest.getToken(),
                    accountActivationRequest.getPassword(),
                    request.getRemoteAddr(),
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
