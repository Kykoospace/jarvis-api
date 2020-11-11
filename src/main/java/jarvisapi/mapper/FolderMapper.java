package jarvisapi.mapper;

import jarvisapi.dto.FolderCDTO;
import jarvisapi.dto.FolderDTO;
import jarvisapi.entity.Folder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = { UserMapper.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FolderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "folders", ignore = true)
    Folder toFolder(FolderCDTO folderCDTO);

    FolderDTO toFolderDTO(Folder folder);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "folders", ignore = true)
    void updateModel(FolderDTO folderDTO, @MappingTarget Folder folder);
}
