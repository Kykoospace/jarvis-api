package jarvisapi.dto;

import jarvisapi.entity.SubTask;

import java.util.Date;
import java.util.List;

public class TaskCreateDto {
    private long taskCollectionId;
    
    private String label;
    private String description;
    private int priority;
    private boolean pinned;
    private boolean checked;
    private Date expirationDate;
    private Date remindingDate;
    private List<SubTask> subTasks;
    private List<Long> tags;
    private List<Long> sharedWithUsers;
}
