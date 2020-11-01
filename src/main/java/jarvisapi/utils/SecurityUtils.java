package jarvisapi.utils;

import jarvisapi.payload.response.RemoteInfosResponse;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class SecurityUtils {

    public RemoteInfosResponse getRemoteInfos(HttpServletRequest request) {
        return new RemoteInfosResponse(
                request.getRemoteHost(),
                request.getRemotePort(),
                request.getRemoteAddr(),
                request.getRemoteUser());
    }
}
