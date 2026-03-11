package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.TransactionDTO;
import auca.ac.rw.FinanceTracker.DTO.TransactionRequest;
import auca.ac.rw.FinanceTracker.DTO.TransferRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ITransactionService {

    TransactionDTO createTransaction(UUID userId, TransactionRequest request);

    TransactionDTO getTransactionById(UUID transactionId, UUID userId);

    List<TransactionDTO> getTransactionsByUser(UUID userId);

    Page<TransactionDTO> getTransactionsByUserPaginated(UUID userId, String type, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    List<TransactionDTO> getTransactionsByAccount(UUID accountId, UUID userId);

    List<TransactionDTO> getTransactionsByCategory(UUID categoryId, UUID userId);

    TransactionDTO updateTransaction(UUID transactionId, UUID userId, TransactionRequest request);

    void deleteTransaction(UUID transactionId, UUID userId);

    TransactionDTO restoreTransaction(UUID transactionId, UUID userId);

    List<TransactionDTO> transfer(UUID userId, TransferRequest request);
}
