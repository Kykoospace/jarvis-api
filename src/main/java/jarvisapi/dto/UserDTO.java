package jarvisapi.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UserDTO {

    private long id;

    private boolean admin;
    private String firstName;
    private String lastName;
    private String email;
    private Date subscriptionDate;
}
