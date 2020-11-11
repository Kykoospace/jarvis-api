package jarvisapi.controller;

import jarvisapi.dto.FolderCDTO;
import jarvisapi.dto.FolderDTO;
import jarvisapi.exception.FolderNotFoundException;
import jarvisapi.service.FolderService;
import jarvisapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/v1/files")
public class FilesController {

    @Autowired
    private FolderService folderService;

    @Autowired
    private UserService userService;

    @GetMapping("")
    public ResponseEntity getUserFiles() {
        try {
            FolderDTO folderDTO = this.folderService.getUserFiles();

            return ResponseEntity.status(HttpStatus.OK).body(folderDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/folders")
    public ResponseEntity createFolder(@RequestBody FolderCDTO folderCDTO) {
        try {
            FolderDTO folderDTO = this.folderService.create(folderCDTO);

            return ResponseEntity.status(HttpStatus.OK).body(folderDTO);
        } catch (FolderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/folders")
    public ResponseEntity updateFolder(@RequestBody FolderDTO folderDTO) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(this.folderService.update(folderDTO));
        } catch (FolderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/folders/{id}")
    public ResponseEntity deleteFolder(@PathVariable long id) {
        try {
            this.folderService.delete(id);
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (FolderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
