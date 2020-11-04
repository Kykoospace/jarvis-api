package jarvisapi.mapper;

import jarvisapi.dto.UserDeviceDTO;
import jarvisapi.entity.UserDevice;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = { DeviceConnectionMapper.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserDeviceMapper {
    UserDeviceDTO toUserDTO(UserDevice userDevice);

    UserDevice toUser(UserDeviceDTO userDeviceDTO);

    List<UserDeviceDTO> toUserDTO(List<UserDevice> userDevices);

    void updateModel(UserDeviceDTO userDeviceDTO, @MappingTarget UserDevice userDevice);
}
