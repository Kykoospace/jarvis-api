package jarvisapi.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
public class Connection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserDevice userDevice;

    @Column(name = "date")
    private Date date = new Date();

    @Column(name = "location")
    private String location;

    public Connection(
            UserDevice userDevice,
            String location
    ) {
        this.userDevice = userDevice;
        this.location = location;
    }
}
