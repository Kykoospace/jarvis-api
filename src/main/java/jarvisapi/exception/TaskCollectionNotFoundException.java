package jarvisapi.exception;

public class TaskCollectionNotFoundException extends RuntimeException {
    public TaskCollectionNotFoundException() {
        super("Task collection not found");
    }
}
