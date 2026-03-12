package auca.ac.rw.FinanceTracker.controller;

import auca.ac.rw.FinanceTracker.DTO.ApiResponse;
import auca.ac.rw.FinanceTracker.DTO.ReportDTO;
import auca.ac.rw.FinanceTracker.DTO.ReportRequest;
import auca.ac.rw.FinanceTracker.service.IReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final IReportService reportService;

    public ReportController(IReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<ReportDTO>> generateReport(
            @Valid @RequestBody ReportRequest request,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        ReportDTO report = reportService.generateReport(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Report generated successfully", report));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReportDTO>> getReportById(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        ReportDTO report = reportService.getReportById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReportDTO>>> getMyReports(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<ReportDTO> reports = reportService.getReportsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReport(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        reportService.deleteReport(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Report deleted successfully"));
    }
}

