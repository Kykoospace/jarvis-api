package jarvisapi.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Mail {

    private String mailFrom;
    private String mailTo;
    private String mailCc;
    private String mailBcc;
    private String mailSubject;
    private String mailContent;
    private String contentType;

    private List < Object > attachments;
    private Map < String, Object > model;

    public Mail() {
        this.contentType = "text/plain";
    }
}
