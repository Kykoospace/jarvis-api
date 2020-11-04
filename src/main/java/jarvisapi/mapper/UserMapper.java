package jarvisapi.mapper;

import jarvisapi.dto.UserDTO;
import jarvisapi.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel="spring")
public interface UserMapper {

    UserDTO toUserDTO(User user);

    User toUser(UserDTO userDTO);

    List<UserDTO> toUserDTO(List<User> users);

    List<User> toUser(List<UserDTO> users);

    void updateModel(UserDTO userDTO, @MappingTarget User user);
}
