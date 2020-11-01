package jarvisapi.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RemoteInfosResponse {

    private String host;
    private int port;
    private String address;
    private String user;
}
