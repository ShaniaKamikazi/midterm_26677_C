package auca.ac.rw.FinanceTracker.controller;

import auca.ac.rw.FinanceTracker.DTO.ApiResponse;
import auca.ac.rw.FinanceTracker.DTO.TransactionDTO;
import auca.ac.rw.FinanceTracker.DTO.TransactionRequest;
import auca.ac.rw.FinanceTracker.DTO.TransferRequest;
import auca.ac.rw.FinanceTracker.service.ITransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final ITransactionService transactionService;

    public TransactionController(ITransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionDTO>> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        TransactionDTO transaction = transactionService.createTransaction(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction created successfully", transaction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionDTO>> getTransactionById(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        TransactionDTO transaction = transactionService.getTransactionById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(transaction));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getMyTransactions(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<TransactionDTO> transactions = transactionService.getTransactionsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<TransactionDTO>>> getMyTransactionsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        Page<TransactionDTO> transactions = transactionService.getTransactionsByUserPaginated(userId, type, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/by-account/{accountId}")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getTransactionsByAccount(
            @PathVariable UUID accountId,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<TransactionDTO> transactions = transactionService.getTransactionsByAccount(accountId, userId);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getTransactionsByCategory(
            @PathVariable UUID categoryId,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<TransactionDTO> transactions = transactionService.getTransactionsByCategory(categoryId, userId);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionDTO>> updateTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        TransactionDTO transaction = transactionService.updateTransaction(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Transaction updated successfully", transaction));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        transactionService.deleteTransaction(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Transaction deleted successfully"));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<TransactionDTO>> restoreTransaction(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        TransactionDTO transaction = transactionService.restoreTransaction(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Transaction restored successfully", transaction));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> transfer(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<TransactionDTO> transactions = transactionService.transfer(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transfer completed successfully", transactions));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportTransactionsCsv(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<TransactionDTO> transactions = transactionService.getTransactionsByUser(userId);

        StringBuilder csv = new StringBuilder();
        csv.append("Transaction ID,Account ID,Amount,Date,Description,Type,Category,Created At\n");
        for (TransactionDTO t : transactions) {
            csv.append(escapeCsv(t.getTransactionId().toString())).append(",");
            csv.append(escapeCsv(t.getAccountId().toString())).append(",");
            csv.append(t.getAmount()).append(",");
            csv.append(t.getDate()).append(",");
            csv.append(escapeCsv(t.getDescription() != null ? t.getDescription() : "")).append(",");
            csv.append(escapeCsv(t.getTransactionType())).append(",");
            csv.append(escapeCsv(t.getCategoryName() != null ? t.getCategoryName() : "")).append(",");
            csv.append(t.getCreatedAt()).append("\n");
        }

        byte[] content = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "transactions.csv");

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
