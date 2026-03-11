package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.ReportDTO;
import auca.ac.rw.FinanceTracker.DTO.ReportRequest;
import auca.ac.rw.FinanceTracker.exception.BadRequestException;
import auca.ac.rw.FinanceTracker.exception.ResourceNotFoundException;
import auca.ac.rw.FinanceTracker.exception.UnauthorizedException;
import auca.ac.rw.FinanceTracker.model.Report;
import auca.ac.rw.FinanceTracker.enums.TransactionType;
import auca.ac.rw.FinanceTracker.model.User;
import auca.ac.rw.FinanceTracker.repository.IReportRepository;
import auca.ac.rw.FinanceTracker.repository.ITransactionRepository;
import auca.ac.rw.FinanceTracker.repository.IUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReportService implements IReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final IReportRepository reportRepository;
    private final ITransactionRepository transactionRepository;
    private final IUserRepository userRepository;

    public ReportService(IReportRepository reportRepository,
                         ITransactionRepository transactionRepository,
                         IUserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ReportDTO generateReport(UUID userId, ReportRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Dynamically compute totals from transactions
        BigDecimal totalIncome = transactionRepository.sumByUserAndTypeAndDateBetween(
                userId, TransactionType.INCOME,
                request.getStartDate().atStartOfDay(),
                request.getEndDate().plusDays(1).atStartOfDay());

        BigDecimal totalExpenses = transactionRepository.sumByUserAndTypeAndDateBetween(
                userId, TransactionType.EXPENSE,
                request.getStartDate().atStartOfDay(),
                request.getEndDate().plusDays(1).atStartOfDay());

        Report report = new Report();
        report.setUser(user);
        report.setStartDate(request.getStartDate());
        report.setEndDate(request.getEndDate());
        report.setTotalIncome(totalIncome);
        report.setTotalExpenses(totalExpenses);

        report = reportRepository.save(report);
        log.info("Report generated for user {} from {} to {}", userId, request.getStartDate(), request.getEndDate());
        return toDTO(report);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDTO getReportById(UUID reportId, UUID userId) {
        Report report = reportRepository.findByReportIdAndDeletedFalse(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        verifyOwnership(report, userId);
        return toDTO(report);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportDTO> getReportsByUser(UUID userId) {
        return reportRepository.findByUserUserIdAndDeletedFalse(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteReport(UUID reportId, UUID userId) {
        Report report = reportRepository.findByReportIdAndDeletedFalse(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        verifyOwnership(report, userId);

        report.softDelete();
        reportRepository.save(report);
        log.info("Report soft-deleted: {}", reportId);
    }

    private void verifyOwnership(Report report, UUID userId) {
        if (!report.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this report");
        }
    }

    private ReportDTO toDTO(Report report) {
        BigDecimal net = report.getTotalIncome().subtract(report.getTotalExpenses());
        return new ReportDTO(
                report.getReportId(),
                report.getStartDate(),
                report.getEndDate(),
                report.getTotalIncome(),
                report.getTotalExpenses(),
                net,
                report.getCreatedAt()
        );
    }
}

