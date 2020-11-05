package jarvisapi.payload.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class DeviceActivationTokenValidityRequest {

    @NotNull
    @NotBlank
    private String email;

    @NotNull
    @NotBlank
    private String token;
}
