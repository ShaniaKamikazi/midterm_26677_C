package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.AccountDTO;
import auca.ac.rw.FinanceTracker.DTO.AccountRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IAccountService {

    AccountDTO createAccount(UUID userId, AccountRequest request);

    AccountDTO getAccountById(UUID accountId, UUID userId);

    List<AccountDTO> getAccountsByUser(UUID userId);

    Page<AccountDTO> getAccountsByUserPaginated(UUID userId, Pageable pageable);

    AccountDTO updateAccount(UUID accountId, UUID userId, AccountRequest request);

    void deleteAccount(UUID accountId, UUID userId);

    AccountDTO restoreAccount(UUID accountId, UUID userId);
}
