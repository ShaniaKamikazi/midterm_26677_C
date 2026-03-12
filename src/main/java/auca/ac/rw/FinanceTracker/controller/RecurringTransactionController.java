package auca.ac.rw.FinanceTracker.controller;

import auca.ac.rw.FinanceTracker.DTO.ApiResponse;
import auca.ac.rw.FinanceTracker.DTO.RecurringTransactionDTO;
import auca.ac.rw.FinanceTracker.DTO.RecurringTransactionRequest;
import auca.ac.rw.FinanceTracker.service.IRecurringTransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/recurring-transactions")
public class RecurringTransactionController {

    private final IRecurringTransactionService recurringService;

    public RecurringTransactionController(IRecurringTransactionService recurringService) {
        this.recurringService = recurringService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RecurringTransactionDTO>> create(
            @Valid @RequestBody RecurringTransactionRequest request,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        RecurringTransactionDTO dto = recurringService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Recurring transaction created successfully", dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RecurringTransactionDTO>> getById(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        RecurringTransactionDTO dto = recurringService.getById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RecurringTransactionDTO>>> getMyRecurring(
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<RecurringTransactionDTO> list = recurringService.getByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RecurringTransactionDTO>> update(
            @PathVariable UUID id,
            @Valid @RequestBody RecurringTransactionRequest request,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        RecurringTransactionDTO dto = recurringService.update(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Recurring transaction updated successfully", dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        recurringService.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Recurring transaction deleted successfully"));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        recurringService.deactivate(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Recurring transaction deactivated"));
    }
}
