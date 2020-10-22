package jarvisapi.service;

import jarvisapi.entity.Task;
import jarvisapi.entity.User;
import jarvisapi.exception.TaskNotFoundException;
import jarvisapi.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskCollectionService taskCollectionService;

    public List<Task> getTasks() {
        return this.taskRepository.findAll();
    }

    public Task getTask(long id) throws TaskNotFoundException {
        Optional<Task> task = this.taskRepository.findById(id);

        if (!task.isPresent()) {
            throw new TaskNotFoundException();
        }

        return task.get();
    }

    public List<Task> getUserTasks(User user) {
        return this.taskRepository.findAllByUser(user);
    }

    public Task getUserTask(User user, long idTask) throws TaskNotFoundException {
        Optional<Task> task = this.taskRepository.findOneByUser(user, idTask);

        if (!task.isPresent()) {
            throw new TaskNotFoundException();
        }

        return task.get();
    }

    public Task createUserTask(Task task, User user) {
        task.setOwner(user);

        return this.taskRepository.save(task);
    }

    public void deleteUserTask(User user, long idTask) throws TaskNotFoundException {
        Task task = this.getUserTask(user, idTask);
        this.taskRepository.delete(task);
    }
}
