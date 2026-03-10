package auca.ac.rw.FinanceTracker.repository;

import auca.ac.rw.FinanceTracker.enums.LocationType;
import auca.ac.rw.FinanceTracker.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ILocationRepository extends JpaRepository<Location, UUID> {

    List<Location> findByLocationType(LocationType locationType);

    List<Location> findByParent_LocationId(UUID parentId);

    List<Location> findByParentIsNull();
}
