package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.AccountDTO;
import auca.ac.rw.FinanceTracker.DTO.AccountRequest;
import auca.ac.rw.FinanceTracker.enums.FinancialAccountType;
import auca.ac.rw.FinanceTracker.exception.ResourceNotFoundException;
import auca.ac.rw.FinanceTracker.exception.UnauthorizedException;
import auca.ac.rw.FinanceTracker.model.Account;
import auca.ac.rw.FinanceTracker.model.User;
import auca.ac.rw.FinanceTracker.repository.IAccountRepository;
import auca.ac.rw.FinanceTracker.repository.IUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountService implements IAccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final IAccountRepository accountRepository;
    private final IUserRepository userRepository;

    public AccountService(IAccountRepository accountRepository, IUserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public AccountDTO createAccount(UUID userId, AccountRequest request) {
        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Account account = new Account();
        account.setUser(user);
        account.setAccountName(request.getAccountName());
        account.setBalance(request.getBalance());
        account.setFinancialAccountType(request.getFinancialAccountType() != null
                ? FinancialAccountType.valueOf(request.getFinancialAccountType().toUpperCase())
                : FinancialAccountType.CASH);

        account = accountRepository.save(account);
        log.info("Account created for user: {}", userId);
        return toDTO(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDTO getAccountById(UUID accountId, UUID userId) {
        Account account = accountRepository.findByAccountIdAndDeletedFalse(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        verifyOwnership(account, userId);
        return toDTO(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountDTO> getAccountsByUser(UUID userId) {
        return accountRepository.findByUserUserIdAndDeletedFalse(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountDTO> getAccountsByUserPaginated(UUID userId, Pageable pageable) {
        return accountRepository.findByUserUserIdAndDeletedFalse(userId, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional
    public void deleteAccount(UUID accountId, UUID userId) {
        Account account = accountRepository.findByAccountIdAndDeletedFalse(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        verifyOwnership(account, userId);

        account.softDelete();
        accountRepository.save(account);
        log.info("Account soft-deleted: {}", accountId);
    }

    @Override
    @Transactional
    public AccountDTO updateAccount(UUID accountId, UUID userId, AccountRequest request) {
        Account account = accountRepository.findByAccountIdAndDeletedFalse(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        verifyOwnership(account, userId);

        account.setAccountName(request.getAccountName());
        if (request.getFinancialAccountType() != null) {
            account.setFinancialAccountType(FinancialAccountType.valueOf(request.getFinancialAccountType().toUpperCase()));
        }
        account = accountRepository.save(account);
        log.info("Account updated: {}", accountId);
        return toDTO(account);
    }

    @Override
    @Transactional
    public AccountDTO restoreAccount(UUID accountId, UUID userId) {
        Account account = accountRepository.findByAccountIdAndDeletedTrue(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Deleted account not found"));
        verifyOwnership(account, userId);

        account.restore();
        account = accountRepository.save(account);
        log.info("Account restored: {}", accountId);
        return toDTO(account);
    }

    private void verifyOwnership(Account account, UUID userId) {
        if (!account.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this account");
        }
    }

    private AccountDTO toDTO(Account account) {
        return new AccountDTO(
                account.getAccountId(),
                account.getAccountName(),
                account.getFinancialAccountType() != null ? account.getFinancialAccountType().name() : null,
                account.getBalance(),
                account.getCreatedAt()
        );
    }
}

