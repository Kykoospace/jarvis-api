package jarvisapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private TaskCollection collection;

    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "description")
    private String description;

    @Column(name = "priority")
    private int priority;

    @Column(name = "pinned")
    private boolean pinned;

    @Column(name = "checked")
    private boolean checked;

    @Column(name = "checking_date")
    private Date checkingDate;

    @Column(name = "expiration_date")
    private Date expirationDate;

    @Column(name = "reminding_date")
    private Date remindingDate;

    @Column(name = "creation_date", nullable = false)
    private Date creationDate = new Date();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "task", fetch = FetchType.LAZY)
    private List<SubTask> subTasks;

    @ManyToMany
    @JoinTable(name = "jt_task_has_tag", joinColumns = @JoinColumn(name = "task_id"), inverseJoinColumns = @JoinColumn(name = "task_tag_id"))
    private List<TaskTag> tags;

    @ManyToMany
    @JoinTable(name = "jt_task_is_shared", joinColumns = @JoinColumn(name = "task_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> sharedWithUsers;

    public Task(
            TaskCollection collection,
            String label,
            String description,
            int priority,
            Date expirationDate,
            Date remindingDate,
            boolean pinned,
            boolean checked,
            List<SubTask> subTasks,
            List<TaskTag> tags,
            List<User> sharedWithUsers
    ) {
        this.collection = collection;
        this.label = label;
        this.description = description;
        this.priority = priority;
        this.expirationDate = expirationDate;
        this.remindingDate = remindingDate;
        this.pinned = pinned;
        this.checked = checked;
        this.checkingDate = (checked) ? new Date() : null;
        this.subTasks = subTasks;
        this.tags = tags;
        this.sharedWithUsers = sharedWithUsers;
    }
}
