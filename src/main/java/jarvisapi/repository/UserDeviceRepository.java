package jarvisapi.repository;

import jarvisapi.entity.User;
import jarvisapi.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    @Query("SELECT d FROM User u INNER JOIN u.userSecurity s INNER JOIN s.devices d WHERE u.email=:userEmail AND d.publicIp=:publicIp")
    Optional<UserDevice> getByUserEmailAndPublicIp(@Param("userEmail") String userEmail, @Param("publicIp") String publicIp);
}
