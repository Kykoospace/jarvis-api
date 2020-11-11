package jarvisapi.repository;

import jarvisapi.entity.Folder;
import jarvisapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    @Query("SELECT f FROM Folder f WHERE f.id=:id AND f.owner=:owner AND f.parent<>NULL")
    Optional<Folder> getByIdAndOwnerId(@Param("id") long id, @Param("owner") User owner);
}
