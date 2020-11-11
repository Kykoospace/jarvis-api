package jarvisapi.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class FolderDTO {

    private long id;

    private String name;
    private Date creationDate;
    private UserShortDTO owner;
    private List<FolderDTO> folders;
}
