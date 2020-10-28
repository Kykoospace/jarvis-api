package jarvisapi.dto;

import jarvisapi.entity.SubTask;
import jarvisapi.entity.TaskTag;

import java.util.Date;
import java.util.List;

public class TaskDto {
    private long id;
    private long taskCollectionId;
    private long ownerId;

    private String label;
    private String description;
    private int priority;
    private boolean pinned;
    private boolean checked;
    private Date checkingDate;
    private Date expirationDate;
    private Date remindingDate;
    private Date creationDate;
    private List<SubTask> subTasks;
    private List<TaskTag> tags;
    private List<Long> sharedWithUsers;
}
