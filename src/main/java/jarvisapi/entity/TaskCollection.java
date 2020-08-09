package jarvisapi.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
public class TaskCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "color")
    private String color;

    @Column(name = "deletable", nullable = false)
    private boolean deletable;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "collection", fetch = FetchType.LAZY)
    private List<Task> tasks;

    public TaskCollection(
            User owner,
            String label,
            String color,
            boolean deletable,
            List<Task> tasks
    ) {
        this.owner = owner;
        this.label = label;
        this.color = color;
        this.deletable = deletable;
        this.tasks = tasks;
    }
}
