package jarvisapi.service;

import com.sun.mail.util.MailConnectException;
import freemarker.template.TemplateException;
import jarvisapi.entity.SignUpRequest;
import jarvisapi.entity.SingleUseToken;
import jarvisapi.entity.User;
import jarvisapi.exception.SignUpRequestNotFoundException;
import jarvisapi.repository.SignUpRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class SignUpRequestService {

    @Autowired
    private SignUpRequestRepository signUpRequestRepository;

    @Autowired
    private UserService userService;

    /**
     * Get all SignUpRequest
     * @return
     * @throws SignUpRequestNotFoundException
     */
    public List<SignUpRequest> getAll() throws SignUpRequestNotFoundException {
        return this.signUpRequestRepository.findAll();
    }

    /**
     * Get SignUpRequest
     * @param id
     * @return
     * @throws SignUpRequestNotFoundException
     */
    public SignUpRequest get(long id) throws SignUpRequestNotFoundException {
        Optional<SignUpRequest> signUpRequest = this.signUpRequestRepository.findById(id);

        if (!signUpRequest.isPresent()) {
            throw new SignUpRequestNotFoundException();
        }

        return signUpRequest.get();
    }

    /**
     * Create SignUpRequest
     * @param firstName
     * @param lastName
     * @param email
     * @return
     */
    public SignUpRequest create(String firstName, String lastName, String email) {
        SignUpRequest signUpRequest = new SignUpRequest(firstName, lastName, email);
        return this.signUpRequestRepository.save(signUpRequest);
    }

    /**
     * Check if email is available
     * @param email
     * @return
     */
    public boolean isEmailAvailable(String email) {
        Optional<SignUpRequest> signUpRequest = this.signUpRequestRepository.findByEmail(email);
        return !signUpRequest.isPresent();
    }

    /**
     * Accept SignUpRequest
     * @param signUpRequestId
     * @throws SignUpRequestNotFoundException
     */
    public void accept(long signUpRequestId) throws SignUpRequestNotFoundException, MessagingException, IOException, TemplateException {
        Optional<SignUpRequest> signUpRequestOptional = this.signUpRequestRepository.findById(signUpRequestId);

        if (!signUpRequestOptional.isPresent()) {
            throw new SignUpRequestNotFoundException();
        }

        // Create the new user:
        SignUpRequest signUpRequest = signUpRequestOptional.get();
        User user = this.userService.create(
                signUpRequest.getFirstName(),
                signUpRequest.getLastName(),
                signUpRequest.getEmail()
        );

        // Set the activation token:
        this.userService.setNewActivationToken(user.getEmail());

        this.signUpRequestRepository.delete(signUpRequest);
    }

    /**
     * Reject SignUpRequest
     * @param id
     * @throws SignUpRequestNotFoundException
     */
    public void reject(long id) throws SignUpRequestNotFoundException {
        Optional<SignUpRequest> signUpRequest = this.signUpRequestRepository.findById(id);

        if (!signUpRequest.isPresent()) {
            throw new SignUpRequestNotFoundException();
        }

        this.signUpRequestRepository.delete(signUpRequest.get());
    }
}
