package jarvisapi.controller;

import jarvisapi.dto.UserDTO;
import jarvisapi.entity.DeviceConnection;
import jarvisapi.entity.SignUpRequest;
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

import javax.servlet.http.HttpServletRequest;
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
            this.userService.setDeviceCredentialConnectionSuccessful(deviceConnection);

            // User trust device validation:
            UserDTO userDTO = this.userService.getByUsername(authentication.getName());
            this.userService.checkUserDevice(authentication.getName(), httpServletRequest.getRemoteAddr());

            // Updating connection success if device is verified:
            this.userService.setDeviceConnectionSuccessful(deviceConnection);

            SignInResponse signInResponse = new SignInResponse(
                    jwtTokenUtil.generateToken(authentication),
                    "",
                    userDTO.isAdmin(),
                    userDTO);

            return ResponseEntity.status(HttpStatus.OK).body(signInResponse);
        } catch (UserNotFoundException | AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (UserDeviceNotAuthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
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
    public ResponseEntity accountActivation(@RequestBody AccountActivationRequest accountActivationRequest, HttpServletRequest request) {
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
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
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
        } catch (UserNotFoundException | SingleUseTokenNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/activate-account/new-token")
    public ResponseEntity newAccountActivationToken(@RequestBody NewAccountActivationTokenRequest newAccountActivationTokenRequest) {
        try {
            this.userService.requestNewActivationToken(newAccountActivationTokenRequest.getEmail());
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (UserNotFoundException | UserAccountDisabledException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/activate-device")
    public ResponseEntity activateDevice(@Valid @RequestBody DeviceActivationRequest deviceActivationRequest) {
        try {
            this.userService.activateDevice(
                    deviceActivationRequest.getEmail(),
                    deviceActivationRequest.getToken());
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (UserNotFoundException | UserDeviceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (SingleUseTokenExpiredException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/activate-device/check-token")
    public ResponseEntity deviceActivationCheckTokenValidity(@RequestBody DeviceActivationTokenValidityRequest deviceActivationTokenValidityRequest) {
        try {
            boolean isTokenValid = this.userService.checkDeviceActivationTokenValidity(
                    deviceActivationTokenValidityRequest.getEmail(),
                    deviceActivationTokenValidityRequest.getToken());

            return ResponseEntity.status(HttpStatus.OK).body(isTokenValid);
        } catch (UserNotFoundException | UserDeviceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
