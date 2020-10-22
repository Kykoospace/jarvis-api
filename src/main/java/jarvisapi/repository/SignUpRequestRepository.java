package jarvisapi.repository;

import jarvisapi.entity.SignUpRequest;
import jarvisapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SignUpRequestRepository extends JpaRepository<SignUpRequest, Long> {
    Optional<SignUpRequest> findByEmail(String email);
}
