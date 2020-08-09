package jarvisapi.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
public class SubTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch= FetchType.LAZY)
    private Task task;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "description")
    private String description;

    @Column(name = "priority")
    private int priority;

    @Column(name = "expiration_date")
    private Date expirationDate;

    @Column(name = "reminding_date")
    private Date remindingDate;

    @Column(name = "checked")
    private Boolean checked;

    @Column(name = "checked_date")
    private Date checkedDate;

    @Column(name = "creation_date")
    private Date creationDate;

    public SubTask(
            Task task,
            String label,
            String description,
            int priority,
            Date expirationDate,
            Date remindingDate,
            boolean checked
    ) {
        this.task = task;
        this.label = label;
        this.description = description;
        this.priority = priority;
        this.expirationDate = expirationDate;
        this.remindingDate = remindingDate;
        this.checked = checked;
        this.checkedDate = (checked) ? new Date() : null;
        this.creationDate = new Date();
    }
}
