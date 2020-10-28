package jarvisapi.exception;

public class SingleUseTokenNotFoundException extends RuntimeException {
    public SingleUseTokenNotFoundException() {
        super("Single use token not found");
    }
}
