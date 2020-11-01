package jarvisapi.repository;

import jarvisapi.entity.DeviceConnection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceConnectionRepository extends JpaRepository<DeviceConnection, Long> {
}
