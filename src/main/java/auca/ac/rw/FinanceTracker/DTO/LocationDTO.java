package auca.ac.rw.FinanceTracker.DTO;

import auca.ac.rw.FinanceTracker.enums.LocationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDTO {
    private UUID locationId;
    private String name;
    private String code;
    private LocationType locationType;
    private UUID parentId;
}
