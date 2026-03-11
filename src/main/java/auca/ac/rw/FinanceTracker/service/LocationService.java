package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.LocationDTO;
import auca.ac.rw.FinanceTracker.enums.LocationType;
import auca.ac.rw.FinanceTracker.exception.ResourceNotFoundException;
import auca.ac.rw.FinanceTracker.model.Location;
import auca.ac.rw.FinanceTracker.repository.ILocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LocationService implements ILocationService {

    private final ILocationRepository locationRepository;

    public LocationService(ILocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationDTO> getByType(LocationType type) {
        return locationRepository.findByLocationType(type)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationDTO> getChildren(UUID parentId) {
        return locationRepository.findByParent_LocationId(parentId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationDTO> getProvinces() {
        return locationRepository.findByLocationType(LocationType.PROVINCE)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LocationDTO getById(UUID id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
        return toDTO(location);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationDTO> getLocationHierarchy(UUID villageId) {
        List<LocationDTO> hierarchy = new ArrayList<>();
        Location current = locationRepository.findById(villageId)
                .orElseThrow(() -> new ResourceNotFoundException("Village not found"));

        while (current != null) {
            hierarchy.add(toDTO(current));
            current = current.getParent();
        }
        Collections.reverse(hierarchy);
        return hierarchy;
    }

    private LocationDTO toDTO(Location location) {
        return new LocationDTO(
                location.getLocationId(),
                location.getName(),
                location.getCode(),
                location.getLocationType(),
                location.getParent() != null ? location.getParent().getLocationId() : null
        );
    }
}
