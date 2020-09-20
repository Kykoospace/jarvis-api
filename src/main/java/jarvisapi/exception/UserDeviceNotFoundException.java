package jarvisapi.exception;

public class UserDeviceNotFoundException extends RuntimeException {
    public UserDeviceNotFoundException() {
        super("User device not found");
    }
}
