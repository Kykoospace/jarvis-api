package jarvisapi.mapper;

import jarvisapi.dto.UserDTO;
import jarvisapi.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel="spring")
public abstract class UserMapper {

    public abstract UserDTO toUserDTO(User user);

    public abstract User toUser(UserDTO userDTO);

    public abstract List<UserDTO> toUserDTO(List<User> users);

    public abstract List<User> toUser(List<UserDTO> users);

    public abstract void updateModel(UserDTO userDTO, @MappingTarget User user);


    @AfterMapping
    protected void fillAdmin(User user, @MappingTarget UserDTO userDTO) {
        userDTO.setAdmin(user.getUserSecurity().isAdmin());
    }
}
