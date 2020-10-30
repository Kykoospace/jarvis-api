package jarvisapi.controller;


import ch.qos.logback.core.net.SyslogOutputStream;
import jarvisapi.entity.Task;
import jarvisapi.entity.User;
import jarvisapi.exception.TaskNotFoundException;
import jarvisapi.exception.UserNotFoundException;
import jarvisapi.service.TaskService;
import jarvisapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @GetMapping("")
    public ResponseEntity getUserTasks() {
        User user = this.userService.getUserFromContext();
        List<Task> tasks = this.taskService.getUserTasks(user);

        return ResponseEntity.status(HttpStatus.OK).body(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity getUserTask(@PathVariable long idTask) {
        try {
            User user = this.userService.getUserFromContext();
            Task task = this.taskService.getUserTask(user, idTask);

            return ResponseEntity.status(HttpStatus.OK).body(task);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (TaskNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("")
    public ResponseEntity createUserTask(@RequestBody Task task) {
        try {
            User user = this.userService.getUserFromContext();

            return ResponseEntity.status(HttpStatus.OK).body(this.taskService.createUserTask(task, user));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteUserTask(@PathVariable long idTask) {
        try {
            User user = this.userService.getUserFromContext();
            this.taskService.deleteUserTask(user, idTask);
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (TaskNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
