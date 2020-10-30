package jarvisapi.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserSecurity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(mappedBy = "userSecurity")
    private User user;

    @Column(name = "is_administrator")
    private boolean isAdmin = false;

    @Column(name = "password")
    private String password;

    @Column(name = "account_enabled")
    private boolean accountEnabled = false;

    @OneToOne(cascade = CascadeType.ALL)
    private SingleUseToken accountValidationToken;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userSecurity", fetch = FetchType.LAZY)
    private List<UserDevice> devices;

    public UserSecurity(
            String password
    ) {
        this.password = password;
    }
}
