package jarvisapi.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
public class SubTask {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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
    private Date reminding_date;

    @Column(name = "checked")
    private Boolean checked;

    @Column(name = "checked_date")
    private Date checkedDate;
}
