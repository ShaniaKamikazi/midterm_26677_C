package auca.ac.rw.FinanceTracker.controller;

import auca.ac.rw.FinanceTracker.DTO.ApiResponse;
import auca.ac.rw.FinanceTracker.DTO.InsightsDTO;
import auca.ac.rw.FinanceTracker.service.IInsightsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/insights")
public class InsightsController {

    private final IInsightsService insightsService;

    public InsightsController(IInsightsService insightsService) {
        this.insightsService = insightsService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<InsightsDTO>> getMonthlyInsights(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        InsightsDTO insights = insightsService.getMonthlyInsights(userId);
        return ResponseEntity.ok(ApiResponse.success(insights));
    }
}
