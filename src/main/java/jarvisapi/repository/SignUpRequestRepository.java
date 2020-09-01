package jarvisapi.repository;

import jarvisapi.entity.SignUpRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignUpRequestRepository extends JpaRepository<SignUpRequest, Long> { }
