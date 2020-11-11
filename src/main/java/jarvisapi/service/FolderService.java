package jarvisapi.service;

import jarvisapi.dto.FolderCDTO;
import jarvisapi.dto.FolderDTO;
import jarvisapi.entity.Folder;
import jarvisapi.entity.User;
import jarvisapi.exception.FolderNotFoundException;
import jarvisapi.mapper.FolderMapper;
import jarvisapi.repository.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FolderService {

    @Autowired
    private FolderMapper folderMapper;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private UserService userService;

    public static final String DEFAULT_HOME_FOLDER_NAME = "home";


    public FolderDTO getUserFiles() {
        User user = this.userService.getUserFromContext();

        return this.folderMapper.toFolderDTO(user.getHomeFolder());
    }

    public FolderDTO create(FolderCDTO folderCDTO) {
        User user = this.userService.getUserFromContext();
        Folder folder = this.folderMapper.toFolder(folderCDTO);

        Optional<Folder> parent = this.folderRepository.getByIdAndOwnerId(folderCDTO.getParentId(), user);

        if (!parent.isPresent()) {
            throw new FolderNotFoundException();
        }

        folder.setOwner(user);
        folder.setParent(parent.get());
        this.folderRepository.save(folder);
        return this.folderMapper.toFolderDTO(folder);
    }

    public FolderDTO update(FolderDTO folderDTO) {
        User user = this.userService.getUserFromContext();
        Optional<Folder> folderOpt = this.folderRepository.getByIdAndOwnerId(folderDTO.getId(), user);

        if (!folderOpt.isPresent()) {
            throw new FolderNotFoundException();
        }

        Folder folder = folderOpt.get();

        this.folderMapper.updateModel(folderDTO, folder);
        this.folderRepository.save(folder);
        return this.folderMapper.toFolderDTO(folder);
    }

    public void delete(long id) {
        User user = this.userService.getUserFromContext();
        Optional<Folder> folderOpt = this.folderRepository.getByIdAndOwnerId(id, user);

        if (!folderOpt.isPresent()) {
            throw new FolderNotFoundException();
        }

        this.folderRepository.delete(folderOpt.get());
    }

    public Folder createHomeFolder(User user) {
        Folder folder = new Folder();
        folder.setOwner(user);
        folder.setName(DEFAULT_HOME_FOLDER_NAME);
        return this.folderRepository.save(folder);
    }
}
