package jarvisapi.service;

import freemarker.template.TemplateException;
import jarvisapi.entity.SignUpRequest;
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
     */
    public List<SignUpRequest> getAll() {
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
     * Accept sign up request and send account activation email
     * @param signUpRequestId
     * @throws SignUpRequestNotFoundException
     * @throws MessagingException
     * @throws IOException
     * @throws TemplateException
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
        this.userService.setActivationToken(user);
        this.userService.sendAccountActivationEmail(user);

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
