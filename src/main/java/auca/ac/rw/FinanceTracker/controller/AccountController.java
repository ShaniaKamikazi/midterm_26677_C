package auca.ac.rw.FinanceTracker.controller;

import auca.ac.rw.FinanceTracker.DTO.AccountDTO;
import auca.ac.rw.FinanceTracker.DTO.AccountRequest;
import auca.ac.rw.FinanceTracker.DTO.ApiResponse;
import auca.ac.rw.FinanceTracker.service.IAccountService;
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
@RequestMapping("/api/accounts")
public class AccountController {

    private final IAccountService accountService;

    public AccountController(IAccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountDTO>> createAccount(
            @Valid @RequestBody AccountRequest request,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        AccountDTO account = accountService.createAccount(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", account));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountDTO>> getAccountById(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        AccountDTO account = accountService.getAccountById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(account));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountDTO>>> getMyAccounts(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<AccountDTO> accounts = accountService.getAccountsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<AccountDTO>>> getMyAccountsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AccountDTO> accounts = accountService.getAccountsByUserPaginated(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        accountService.deleteAccount(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountDTO>> updateAccount(
            @PathVariable UUID id,
            @Valid @RequestBody AccountRequest request,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        AccountDTO account = accountService.updateAccount(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Account updated successfully", account));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<AccountDTO>> restoreAccount(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        AccountDTO account = accountService.restoreAccount(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Account restored successfully", account));
    }
}

