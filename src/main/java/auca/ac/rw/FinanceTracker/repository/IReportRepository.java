package auca.ac.rw.FinanceTracker.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import auca.ac.rw.FinanceTracker.model.Report;

@Repository
public interface IReportRepository extends JpaRepository<Report, UUID> {

    List<Report> findByUserUserIdAndDeletedFalse(UUID userId);

    Optional<Report> findByReportIdAndDeletedFalse(UUID reportId);
}
