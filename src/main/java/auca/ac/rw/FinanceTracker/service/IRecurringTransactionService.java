package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.RecurringTransactionDTO;
import auca.ac.rw.FinanceTracker.DTO.RecurringTransactionRequest;

import java.util.List;
import java.util.UUID;

public interface IRecurringTransactionService {

    RecurringTransactionDTO create(UUID userId, RecurringTransactionRequest request);

    RecurringTransactionDTO getById(UUID id, UUID userId);

    List<RecurringTransactionDTO> getByUser(UUID userId);

    RecurringTransactionDTO update(UUID id, UUID userId, RecurringTransactionRequest request);

    void delete(UUID id, UUID userId);

    void deactivate(UUID id, UUID userId);

    void processRecurringTransactions();
}
