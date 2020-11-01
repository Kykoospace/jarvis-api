package jarvisapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class SubTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JsonIgnore
    @ManyToOne(fetch= FetchType.LAZY)
    private Task task;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "checked")
    private Boolean checked;

    @Column(name = "check_date")
    private Date checkDate;

    public SubTask(
            Task task,
            String label,
            boolean checked
    ) {
        this.task = task;
        this.label = label;
        this.checked = checked;
        this.checkDate = (checked) ? new Date() : null;
    }
}
