package auca.ac.rw.FinanceTracker.controller;

import auca.ac.rw.FinanceTracker.DTO.ApiResponse;
import auca.ac.rw.FinanceTracker.DTO.LocationDTO;
import auca.ac.rw.FinanceTracker.enums.LocationType;
import auca.ac.rw.FinanceTracker.service.ILocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final ILocationService locationService;

    public LocationController(ILocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/provinces")
    public ResponseEntity<ApiResponse<List<LocationDTO>>> getProvinces() {
        return ResponseEntity.ok(ApiResponse.success("Provinces retrieved", locationService.getProvinces()));
    }

    @GetMapping("/children/{parentId}")
    public ResponseEntity<ApiResponse<List<LocationDTO>>> getChildren(@PathVariable UUID parentId) {
        return ResponseEntity.ok(ApiResponse.success("Children retrieved", locationService.getChildren(parentId)));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<LocationDTO>>> getByType(@PathVariable LocationType type) {
        return ResponseEntity.ok(ApiResponse.success("Locations retrieved", locationService.getByType(type)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LocationDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Location retrieved", locationService.getById(id)));
    }

    @GetMapping("/hierarchy/{villageId}")
    public ResponseEntity<ApiResponse<List<LocationDTO>>> getHierarchy(@PathVariable UUID villageId) {
        return ResponseEntity.ok(ApiResponse.success("Hierarchy retrieved", locationService.getLocationHierarchy(villageId)));
    }
}
