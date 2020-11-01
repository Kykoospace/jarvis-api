package jarvisapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DeviceConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private UserDevice userDevice;

    @Column(name = "success")
    private boolean success;

    @CreationTimestamp
    @Column(name = "date")
    private Date date;

    @Column(name = "browser")
    private String browser;

    public DeviceConnection(
            UserDevice userDevice,
            String browser
    ) {
        this.userDevice = userDevice;
        this.success = false;
        this.browser = browser;
    }

    public DeviceConnection(
            UserDevice userDevice,
            boolean success,
            String browser
    ) {
        this.userDevice = userDevice;
        this.success = success;
        this.browser = browser;
    }
}
