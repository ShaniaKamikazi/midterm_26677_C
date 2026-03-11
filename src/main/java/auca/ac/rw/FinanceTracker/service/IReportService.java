package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.ReportDTO;
import auca.ac.rw.FinanceTracker.DTO.ReportRequest;

import java.util.List;
import java.util.UUID;

public interface IReportService {

    ReportDTO generateReport(UUID userId, ReportRequest request);

    ReportDTO getReportById(UUID reportId, UUID userId);

    List<ReportDTO> getReportsByUser(UUID userId);

    void deleteReport(UUID reportId, UUID userId);
}
