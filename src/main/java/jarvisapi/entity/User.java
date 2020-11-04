package jarvisapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @CreationTimestamp
    @Column(name = "subscription_date")
    private Date subscriptionDate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_security", referencedColumnName = "id", nullable = false)
    private UserSecurity userSecurity;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.LAZY)
    private List<Task> tasks;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.LAZY)
    private List<TaskCollection> taskCollections;

    @ManyToMany(mappedBy = "sharedWithUsers")
    private List<Task> tasksSharedToUser;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.LAZY)
    private List<TaskTag> taskTags;

    public User(
            String firstName,
            String lastName,
            String email,
            UserSecurity userSecurity
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.userSecurity = userSecurity;
    }
}
