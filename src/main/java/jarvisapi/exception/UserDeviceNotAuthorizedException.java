package jarvisapi.exception;

public class UserDeviceNotAuthorizedException extends RuntimeException {
    public UserDeviceNotAuthorizedException() {
        super("User device not authorized");
    }
}
