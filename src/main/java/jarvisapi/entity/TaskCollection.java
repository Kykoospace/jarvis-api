package jarvisapi.entity;

import javax.persistence.*;
import java.util.List;

@Entity
public class TaskCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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
}
