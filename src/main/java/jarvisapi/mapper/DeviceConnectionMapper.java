package jarvisapi.mapper;

import jarvisapi.dto.DeviceConnectionDTO;
import jarvisapi.entity.DeviceConnection;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeviceConnectionMapper {
    DeviceConnectionDTO toDTO(DeviceConnection deviceConnection);

    DeviceConnection toEntity(DeviceConnectionDTO deviceConnectionDTO);

    List<DeviceConnectionDTO> toDTO(List<DeviceConnection> deviceConnections);

    void updateModel(DeviceConnectionDTO deviceConnectionDTO, @MappingTarget DeviceConnection deviceConnection);
}
