package jarvisapi.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
public class DeviceConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserDevice userDevice;

    @Column(name = "success")
    private boolean success;

    @Column(name = "date")
    private Date date = new Date();

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
