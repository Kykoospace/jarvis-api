package jarvisapi.service;

import jarvisapi.entity.TaskCollection;
import jarvisapi.exception.TaskCollectionNotFoundException;
import jarvisapi.repository.TaskCollectionRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TaskCollectionService {

    private TaskCollectionRepository taskCollectionRepository;

    public TaskCollection getTask(long id) {
        Optional<TaskCollection> taskCollection = this.taskCollectionRepository.findById(id);

        if (taskCollection.isPresent()) {
            throw new TaskCollectionNotFoundException();
        }

        return taskCollection.get();
    }
}
