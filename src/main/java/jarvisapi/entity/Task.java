package jarvisapi.entity;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class Task {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;

    @ManyToOne(fetch= FetchType.LAZY)
    private TaskCollection collection;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "description")
    private String description;

    @Column(name = "priority")
    private int priority;

    @Column(name = "expiration_date")
    private Date expiration_date;

    @Column(name = "reminding_date")
    private Date reminding_date;

    @Column(name = "pinned")
    private boolean pinned;

    @Column(name = "checked")
    private boolean checked;

    @Column(name = "checked_date")
    private Date checkedDate;

    @Column(name = "creation_date")
    private Date creationDate;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "task", fetch = FetchType.LAZY)
    private List<SubTask> subTasks;

    @ManyToMany
    @JoinTable(name = "jt_task_has_tag", joinColumns = @JoinColumn(name = "task_id"), inverseJoinColumns = @JoinColumn(name = "task_tag_id"))
    private List<TaskTag> tags;

    @ManyToMany
    @JoinTable(name = "jt_task_is_shared", joinColumns = @JoinColumn(name = "task_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> sharedWithUsers;
}
