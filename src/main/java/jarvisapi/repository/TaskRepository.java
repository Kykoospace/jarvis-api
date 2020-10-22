package jarvisapi.repository;

import jarvisapi.entity.Task;
import jarvisapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("SELECT t FROM Task t WHERE t.owner = :owner")
    List<Task> findAllByUser(@Param("owner") User user);

    @Query("SELECT t FROM Task t WHERE t.owner = :owner AND t.id = :task_id")
    Optional<Task> findOneByUser(@Param("owner") User user, @Param("task_id") long idTask);
}
