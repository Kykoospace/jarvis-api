package jarvisapi.repository;


import jarvisapi.entity.SingleUseToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SingleUseTokenRepository extends JpaRepository<SingleUseToken, Long> { }
