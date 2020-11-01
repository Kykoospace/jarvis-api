package jarvisapi.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jarvisapi.entity.Mail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
public class MailerService {

    @Value("${spring.front.activateAccountUrl}")
    private String ACTIVATE_ACCOUNT_URL;

    @Value("${spring.mail.username}")
    private String NO_REPLY_EMAIL;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private Configuration freemarkerConfig;

    @Async
    public void sendAccountActivationMail(String userFirstName, String userEmail, String activationToken) throws MessagingException, IOException, TemplateException {
        Mail mail = this.createMail(userEmail, "Account activation");

        // Link construction:
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("email", userEmail);
        urlParams.put("token", activationToken);
        String activationLink = this.encoreUrl(this.ACTIVATE_ACCOUNT_URL, urlParams);

        // Model parameters:
        Map<String, Object> modelParams = new HashMap();
        modelParams.put("userFirstName", userFirstName);
        modelParams.put("activateAccountUrl", activationLink);
        mail.setModel(modelParams);

        this.sendSimpleMessage(mail, "account-activation-email-template.ftl");
    }

    public void sendTrustDeviceVerificationMail(String userFirstName, String userEmail, String verificationToken) throws MessagingException, IOException, TemplateException {
        Mail mail = this.createMail(userEmail, "Device verification");

        // Model parameters:
        Map<String, Object> modelParams = new HashMap();
        modelParams.put("userFirstName", userFirstName);
        modelParams.put("secretCode", verificationToken);
        mail.setModel(modelParams);

        this.sendSimpleMessage(mail, "device-verification-email-template.ftl");
    }

    private void sendSimpleMessage(Mail mail, String emailTemplate) throws MessagingException, IOException, TemplateException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name()
        );

        Template template = freemarkerConfig.getTemplate(emailTemplate);
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, mail.getModel());

        helper.setTo(mail.getMailTo());
        helper.setText(html, true);
        helper.setSubject(mail.getMailSubject());
        helper.setFrom(mail.getMailFrom());

        emailSender.send(message);
    }

    private String encoreUrl(String baseUrl, Map<String, String> params) throws UnsupportedEncodingException {
        String urlEncoded = baseUrl;

        if (params.size() > 0) {
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            Map.Entry<String, String> entry = iterator.next();
            urlEncoded += "?" + entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString());

            while(iterator.hasNext()) {
                entry = iterator.next();
                urlEncoded += "&" + entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString());
            }
        }

        return urlEncoded;
    }

    private Mail createMail(String emailTo, String subject) {
        Mail mail = new Mail();
        mail.setMailFrom(NO_REPLY_EMAIL);
        mail.setMailTo(emailTo);
        mail.setMailSubject(subject);

        return mail;
    }
}
