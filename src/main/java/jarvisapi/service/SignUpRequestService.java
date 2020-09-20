package jarvisapi.service;

import jarvisapi.entity.SignUpRequest;
import jarvisapi.exception.SignUpRequestNotFoundException;
import jarvisapi.repository.SignUpRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SignUpRequestService {

    private static final String DEFAULT_PASSWORD = "banane";

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
     * Accept SignUpRequest
     * @param id
     * @throws SignUpRequestNotFoundException
     */
    public void accept(long id) throws SignUpRequestNotFoundException {
        Optional<SignUpRequest> signUpRequest = this.signUpRequestRepository.findById(id);

        if (!signUpRequest.isPresent()) {
            throw new SignUpRequestNotFoundException();
        }

        SignUpRequest userData = signUpRequest.get();
        this.userService.create(
                userData.getFirstName(),
                userData.getLastName(),
                userData.getEmail(),
                SignUpRequestService.DEFAULT_PASSWORD
        );
        this.signUpRequestRepository.delete(signUpRequest.get());
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
