package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.LocationDTO;
import auca.ac.rw.FinanceTracker.enums.LocationType;

import java.util.List;
import java.util.UUID;

public interface ILocationService {
    List<LocationDTO> getByType(LocationType type);
    List<LocationDTO> getChildren(UUID parentId);
    List<LocationDTO> getProvinces();
    LocationDTO getById(UUID id);
    List<LocationDTO> getLocationHierarchy(UUID villageId);
}
