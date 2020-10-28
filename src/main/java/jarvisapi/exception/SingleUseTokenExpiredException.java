package jarvisapi.exception;

public class SingleUseTokenExpiredException extends RuntimeException {
    public SingleUseTokenExpiredException() {
        super("Single use token is expired");
    }
}
