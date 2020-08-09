package jarvisapi.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
public class TaskTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

    @Column(name = "label", nullable = false)
    private String label;

    @ManyToMany(mappedBy = "tags")
    private List<Task> tasks;

    public TaskTag(
            User owner,
            String label
    ) {
        this.owner = owner;
        this.label = label;
    }
}
