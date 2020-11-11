package jarvisapi.exception;

public class FolderNotFoundException extends RuntimeException {
    public FolderNotFoundException() {
        super("Folder not found");
    }
}
