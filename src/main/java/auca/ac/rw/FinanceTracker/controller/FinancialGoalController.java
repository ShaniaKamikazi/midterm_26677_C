package auca.ac.rw.FinanceTracker.controller;

import auca.ac.rw.FinanceTracker.DTO.ApiResponse;
import auca.ac.rw.FinanceTracker.DTO.FinancialGoalDTO;
import auca.ac.rw.FinanceTracker.DTO.FinancialGoalRequest;
import auca.ac.rw.FinanceTracker.DTO.GoalContributionRequest;
import auca.ac.rw.FinanceTracker.service.IFinancialGoalService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/goals")
public class FinancialGoalController {

    private final IFinancialGoalService goalService;

    public FinancialGoalController(IFinancialGoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FinancialGoalDTO>> createGoal(
            @Valid @RequestBody FinancialGoalRequest request,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        FinancialGoalDTO goal = goalService.createGoal(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Financial goal created successfully", goal));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FinancialGoalDTO>> getGoalById(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        FinancialGoalDTO goal = goalService.getGoalById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(goal));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FinancialGoalDTO>>> getMyGoals(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<FinancialGoalDTO> goals = goalService.getGoalsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(goals));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<FinancialGoalDTO>>> getMyGoalsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("targetDate").ascending());
        Page<FinancialGoalDTO> goals = goalService.getGoalsByUserPaginated(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(goals));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FinancialGoalDTO>> updateGoal(
            @PathVariable UUID id,
            @Valid @RequestBody FinancialGoalRequest request,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        FinancialGoalDTO goal = goalService.updateGoal(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Financial goal updated successfully", goal));
    }

    @PostMapping("/{id}/contribute")
    public ResponseEntity<ApiResponse<FinancialGoalDTO>> contributeToGoal(
            @PathVariable UUID id,
            @Valid @RequestBody GoalContributionRequest request,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        FinancialGoalDTO goal = goalService.contributeToGoal(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Contribution recorded successfully", goal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        goalService.deleteGoal(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Financial goal deleted successfully"));
    }
}
