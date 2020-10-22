package jarvisapi.repository;

import jarvisapi.entity.TaskCollection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskCollectionRepository extends JpaRepository<TaskCollection, Long> {
}
