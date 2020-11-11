package jarvisapi.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "creation_date")
    @CreationTimestamp
    private Date creationDate;

    @ManyToOne
    private User owner;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent")
    private List<Folder> folders;

    @ManyToOne
    private Folder parent;
}
