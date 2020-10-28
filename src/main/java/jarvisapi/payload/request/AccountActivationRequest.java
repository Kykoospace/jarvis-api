package jarvisapi.payload.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class AccountActivationRequest {

    @NotBlank
    @NotNull
    private String email;

    @NotBlank
    @NotNull
    private String activationToken;

    @NotBlank
    @NotNull
    private String password;
}
