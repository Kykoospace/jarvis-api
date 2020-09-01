package jarvisapi.exception;

public class SignUpRequestNotFoundException extends RuntimeException {
    public SignUpRequestNotFoundException() {
        super("Sign-up request not found");
    }
}
