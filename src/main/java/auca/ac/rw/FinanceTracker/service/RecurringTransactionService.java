package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.RecurringTransactionDTO;
import auca.ac.rw.FinanceTracker.DTO.RecurringTransactionRequest;
import auca.ac.rw.FinanceTracker.enums.RecurrenceFrequency;
import auca.ac.rw.FinanceTracker.enums.TransactionType;
import auca.ac.rw.FinanceTracker.exception.BadRequestException;
import auca.ac.rw.FinanceTracker.exception.ResourceNotFoundException;
import auca.ac.rw.FinanceTracker.exception.UnauthorizedException;
import auca.ac.rw.FinanceTracker.model.*;
import auca.ac.rw.FinanceTracker.repository.IAccountRepository;
import auca.ac.rw.FinanceTracker.repository.ICategoryRepository;
import auca.ac.rw.FinanceTracker.repository.IRecurringTransactionRepository;
import auca.ac.rw.FinanceTracker.repository.ITransactionRepository;
import auca.ac.rw.FinanceTracker.repository.IUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RecurringTransactionService implements IRecurringTransactionService {

    private static final Logger log = LoggerFactory.getLogger(RecurringTransactionService.class);

    private final IRecurringTransactionRepository recurringRepo;
    private final ITransactionRepository transactionRepository;
    private final IAccountRepository accountRepository;
    private final ICategoryRepository categoryRepository;
    private final IUserRepository userRepository;

    public RecurringTransactionService(IRecurringTransactionRepository recurringRepo,
                                       ITransactionRepository transactionRepository,
                                       IAccountRepository accountRepository,
                                       ICategoryRepository categoryRepository,
                                       IUserRepository userRepository) {
        this.recurringRepo = recurringRepo;
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public RecurringTransactionDTO create(UUID userId, RecurringTransactionRequest request) {
        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Account account = accountRepository.findByAccountIdAndDeletedFalse(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (!account.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this account");
        }

        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        RecurringTransaction rt = new RecurringTransaction();
        rt.setUser(user);
        rt.setAccount(account);
        rt.setAmount(request.getAmount());
        rt.setDescription(request.getDescription());
        rt.setTransactionType(TransactionType.valueOf(request.getTransactionType().toUpperCase()));
        rt.setFrequency(RecurrenceFrequency.valueOf(request.getFrequency().toUpperCase()));
        rt.setStartDate(request.getStartDate());
        rt.setEndDate(request.getEndDate());
        rt.setNextExecutionDate(request.getStartDate());
        rt.setActive(true);

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findByCategoryIdAndDeletedFalse(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            rt.setCategory(category);
        }

        rt = recurringRepo.save(rt);
        log.info("Recurring transaction created for user {}", userId);
        return toDTO(rt);
    }

    @Override
    @Transactional(readOnly = true)
    public RecurringTransactionDTO getById(UUID id, UUID userId) {
        RecurringTransaction rt = recurringRepo.findByRecurringTransactionIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));
        verifyOwnership(rt, userId);
        return toDTO(rt);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecurringTransactionDTO> getByUser(UUID userId) {
        return recurringRepo.findByUserUserIdAndDeletedFalse(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RecurringTransactionDTO update(UUID id, UUID userId, RecurringTransactionRequest request) {
        RecurringTransaction rt = recurringRepo.findByRecurringTransactionIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));
        verifyOwnership(rt, userId);

        Account account = accountRepository.findByAccountIdAndDeletedFalse(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (!account.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this account");
        }

        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        rt.setAccount(account);
        rt.setAmount(request.getAmount());
        rt.setDescription(request.getDescription());
        rt.setTransactionType(TransactionType.valueOf(request.getTransactionType().toUpperCase()));
        rt.setFrequency(RecurrenceFrequency.valueOf(request.getFrequency().toUpperCase()));
        rt.setStartDate(request.getStartDate());
        rt.setEndDate(request.getEndDate());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findByCategoryIdAndDeletedFalse(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            rt.setCategory(category);
        } else {
            rt.setCategory(null);
        }

        rt = recurringRepo.save(rt);
        log.info("Recurring transaction updated: {}", id);
        return toDTO(rt);
    }

    @Override
    @Transactional
    public void delete(UUID id, UUID userId) {
        RecurringTransaction rt = recurringRepo.findByRecurringTransactionIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));
        verifyOwnership(rt, userId);
        rt.softDelete();
        recurringRepo.save(rt);
        log.info("Recurring transaction soft-deleted: {}", id);
    }

    @Override
    @Transactional
    public void deactivate(UUID id, UUID userId) {
        RecurringTransaction rt = recurringRepo.findByRecurringTransactionIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));
        verifyOwnership(rt, userId);
        rt.setActive(false);
        recurringRepo.save(rt);
        log.info("Recurring transaction deactivated: {}", id);
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *") // runs daily at midnight
    @Transactional
    public void processRecurringTransactions() {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> dueTransactions =
                recurringRepo.findByActiveTrueAndDeletedFalseAndNextExecutionDateLessThanEqual(today);

        for (RecurringTransaction rt : dueTransactions) {
            try {
                Account account = rt.getAccount();
                if (account.isDeleted()) {
                    rt.setActive(false);
                    recurringRepo.save(rt);
                    continue;
                }

                Transaction transaction = new Transaction();
                transaction.setUser(rt.getUser());
                transaction.setAccount(account);
                transaction.setAmount(rt.getAmount());
                transaction.setDescription("[Recurring] " + (rt.getDescription() != null ? rt.getDescription() : ""));
                transaction.setTransactionType(rt.getTransactionType());
                transaction.setDate(LocalDateTime.now());
                transaction.setCategory(rt.getCategory());

                if (rt.getTransactionType() == TransactionType.INCOME) {
                    account.setBalance(account.getBalance().add(rt.getAmount()));
                } else {
                    BigDecimal newBalance = account.getBalance().subtract(rt.getAmount());
                    if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                        log.warn("Skipping recurring transaction {} - insufficient balance", rt.getRecurringTransactionId());
                        continue;
                    }
                    account.setBalance(newBalance);
                }

                accountRepository.save(account);
                transactionRepository.save(transaction);

                // Advance next execution date
                rt.setNextExecutionDate(calculateNextDate(rt.getNextExecutionDate(), rt.getFrequency()));

                // Deactivate if past end date
                if (rt.getEndDate() != null && rt.getNextExecutionDate().isAfter(rt.getEndDate())) {
                    rt.setActive(false);
                }

                recurringRepo.save(rt);
                log.info("Processed recurring transaction: {}", rt.getRecurringTransactionId());
            } catch (Exception e) {
                log.error("Failed to process recurring transaction: {}", rt.getRecurringTransactionId(), e);
            }
        }
    }

    private LocalDate calculateNextDate(LocalDate current, RecurrenceFrequency frequency) {
        return switch (frequency) {
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
            case MONTHLY -> current.plusMonths(1);
            case YEARLY -> current.plusYears(1);
        };
    }

    private void verifyOwnership(RecurringTransaction rt, UUID userId) {
        if (!rt.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this recurring transaction");
        }
    }

    private RecurringTransactionDTO toDTO(RecurringTransaction rt) {
        return new RecurringTransactionDTO(
                rt.getRecurringTransactionId(),
                rt.getAccount().getAccountId(),
                rt.getAccount().getAccountName(),
                rt.getCategory() != null ? rt.getCategory().getCategoryId() : null,
                rt.getCategory() != null ? rt.getCategory().getName() : null,
                rt.getAmount(),
                rt.getDescription(),
                rt.getTransactionType().name(),
                rt.getFrequency().name(),
                rt.getStartDate(),
                rt.getEndDate(),
                rt.getNextExecutionDate(),
                rt.isActive(),
                rt.getCreatedAt()
        );
    }
}
