package jarvisapi.exception;

public class UserAccountEnabledException extends RuntimeException {
    public UserAccountEnabledException() {
        super("User account is disabled");
    }
}
