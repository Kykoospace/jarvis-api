package jarvisapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserShortDTO {
    private long id;

    private String firstName;
    private String lastName;
    private String email;
}
