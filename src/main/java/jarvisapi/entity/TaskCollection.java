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

    @Column(name = "deletable", nullable = false)
    private boolean deletable;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "collection", fetch = FetchType.LAZY)
    private List<Task> tasks;

    public TaskCollection(
            String label,
            boolean deletable
    ) {
        this.label = label;
        this.deletable = deletable;
    }
}
