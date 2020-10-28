package jarvisapi.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class SingleUseToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "token", nullable = false, unique = true)
    private UUID token;

    @Column(name = "expiration_date")
    private Date expirationDate;

    public SingleUseToken(Date expirationDate) {
        this.token = UUID.randomUUID();
        this.expirationDate = expirationDate;
    }
}
