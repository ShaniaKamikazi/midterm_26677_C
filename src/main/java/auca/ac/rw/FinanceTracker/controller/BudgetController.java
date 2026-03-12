package auca.ac.rw.FinanceTracker.controller;

import auca.ac.rw.FinanceTracker.DTO.ApiResponse;
import auca.ac.rw.FinanceTracker.DTO.BudgetDTO;
import auca.ac.rw.FinanceTracker.DTO.BudgetRequest;
import auca.ac.rw.FinanceTracker.service.IBudgetService;
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
@RequestMapping("/api/budgets")
public class BudgetController {

    private final IBudgetService budgetService;

    public BudgetController(IBudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetDTO>> createBudget(
            @Valid @RequestBody BudgetRequest request,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        BudgetDTO budget = budgetService.createBudget(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Budget created successfully", budget));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetDTO>> getBudgetById(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        BudgetDTO budget = budgetService.getBudgetById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(budget));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetDTO>>> getMyBudgets(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<BudgetDTO> budgets = budgetService.getBudgetsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(budgets));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<BudgetDTO>>> getMyBudgetsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").descending());
        Page<BudgetDTO> budgets = budgetService.getBudgetsByUserPaginated(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(budgets));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        budgetService.deleteBudget(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Budget deleted successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetDTO>> updateBudget(
            @PathVariable UUID id,
            @Valid @RequestBody BudgetRequest request,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        BudgetDTO budget = budgetService.updateBudget(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Budget updated successfully", budget));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<BudgetDTO>> restoreBudget(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        BudgetDTO budget = budgetService.restoreBudget(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Budget restored successfully", budget));
    }
}
