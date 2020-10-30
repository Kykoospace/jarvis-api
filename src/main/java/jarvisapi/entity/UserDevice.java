package jarvisapi.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
public class UserDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch= FetchType.LAZY)
    private UserSecurity userSecurity;

    @Column(name = "mac_address")
    private String macAddress;

    @Column(name = "public_ip")
    private String publicIp;

    @Column(name = "type")
    private String type;

    @Column(name = "authorized")
    private boolean authorized = false;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userDevice", fetch = FetchType.LAZY)
    private List<Connection> connections;

    public UserDevice(String macAddress, String publicIp, String type) {
        this.macAddress = macAddress;
        this.publicIp = publicIp;
        this.type = type;
    }
}
