package jarvisapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JsonIgnore
    @ManyToOne
    private UserSecurity userSecurity;

    @Column(name = "public_ip")
    private String publicIp;

    @Column(name = "type")
    private String type;

    @Column(name = "verified")
    private boolean verified = false;

    @Column(name = "verification_date")
    private Date verificationDate;

    @Column(name = "creation_date")
    @CreationTimestamp
    private Date creationDate;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL)
    public SingleUseToken verificationToken;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userDevice")
    private List<DeviceConnection> connections;

    public UserDevice(String publicIp, String type) {
        this.publicIp = publicIp;
        this.type = type;
    }
}
