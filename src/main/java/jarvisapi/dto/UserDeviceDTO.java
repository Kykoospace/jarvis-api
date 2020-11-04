package jarvisapi.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class UserDeviceDTO {

    private long id;

    private String publicIp;
    private String type;
    private boolean verified;
    private Date verificationDate;
    private Date creationDate;
    private List<DeviceConnectionDTO> connections;
}
