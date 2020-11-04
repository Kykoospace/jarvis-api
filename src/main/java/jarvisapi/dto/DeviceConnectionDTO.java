package jarvisapi.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class DeviceConnectionDTO {

    private long id;

    private boolean credentialSuccess;
    private boolean success;
    private Date date;
    private String browser;
}
