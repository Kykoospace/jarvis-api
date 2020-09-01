package jarvisapi.exception;

public class UserSecurityNotFoundException extends RuntimeException {
    public UserSecurityNotFoundException() {
        super("UserSecurity not found");
    }
}
