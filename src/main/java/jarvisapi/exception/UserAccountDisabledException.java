package jarvisapi.exception;

public class UserAccountDisabledException extends RuntimeException {
    public UserAccountDisabledException() {
        super("User account is disabled");
    }
}
