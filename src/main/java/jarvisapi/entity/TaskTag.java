package jarvisapi.entity;

import javax.persistence.*;
import java.util.List;

@Entity
public class TaskTag {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

    private String label;

    @ManyToMany(mappedBy = "tags")
    private List<Task> tasks;
}
