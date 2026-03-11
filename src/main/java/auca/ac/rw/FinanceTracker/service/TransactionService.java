package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.TagDTO;
import auca.ac.rw.FinanceTracker.DTO.TransactionDTO;
import auca.ac.rw.FinanceTracker.DTO.TransactionRequest;
import auca.ac.rw.FinanceTracker.DTO.TransferRequest;
import auca.ac.rw.FinanceTracker.exception.BadRequestException;
import auca.ac.rw.FinanceTracker.exception.ResourceNotFoundException;
import auca.ac.rw.FinanceTracker.exception.UnauthorizedException;
import auca.ac.rw.FinanceTracker.enums.TransactionType;
import auca.ac.rw.FinanceTracker.model.*;
import auca.ac.rw.FinanceTracker.repository.IAccountRepository;
import auca.ac.rw.FinanceTracker.repository.ICategoryRepository;
import auca.ac.rw.FinanceTracker.repository.ITagRepository;
import auca.ac.rw.FinanceTracker.repository.ITransactionRepository;
import auca.ac.rw.FinanceTracker.repository.IUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService implements ITransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final ITransactionRepository transactionRepository;
    private final IAccountRepository accountRepository;
    private final ICategoryRepository categoryRepository;
    private final IUserRepository userRepository;
    private final ITagRepository tagRepository;

    public TransactionService(ITransactionRepository transactionRepository,
                              IAccountRepository accountRepository,
                              ICategoryRepository categoryRepository,
                              IUserRepository userRepository,
                              ITagRepository tagRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    @Transactional
    public TransactionDTO createTransaction(UUID userId, TransactionRequest request) {
        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Account account = accountRepository.findByAccountIdAndDeletedFalse(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this account");
        }

        TransactionType type = TransactionType.valueOf(request.getTransactionType().toUpperCase());

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAccount(account);
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionType(type);
        transaction.setDate(request.getDate() != null ? request.getDate() : LocalDateTime.now());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findByCategoryIdAndDeletedFalse(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            transaction.setCategory(category);
        }

        // Update account balance
        if (type == TransactionType.INCOME) {
            account.setBalance(account.getBalance().add(request.getAmount()));
        } else {
            BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Insufficient balance. Available: " + account.getBalance());
            }
            account.setBalance(newBalance);
        }
        accountRepository.save(account);

        // Handle tags
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> tags = tagRepository.findByTagIdInAndUserUserId(request.getTagIds(), userId);
            transaction.setTags(tags);
        }

        transaction = transactionRepository.save(transaction);
        log.info("Transaction created: {} {} for user {}", type, request.getAmount(), userId);
        return toDTO(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDTO getTransactionById(UUID transactionId, UUID userId) {
        Transaction transaction = transactionRepository.findByTransactionIdAndDeletedFalse(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        verifyOwnership(transaction, userId);
        return toDTO(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByUser(UUID userId) {
        return transactionRepository.findByUserUserIdAndDeletedFalse(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDTO> getTransactionsByUserPaginated(UUID userId, String type, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        TransactionType txType = type != null ? TransactionType.valueOf(type.toUpperCase()) : null;
        return transactionRepository.findByUserFiltered(userId, txType, startDate, endDate, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByAccount(UUID accountId, UUID userId) {
        Account account = accountRepository.findByAccountIdAndDeletedFalse(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (!account.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this account");
        }
        return transactionRepository.findByAccountAccountIdAndDeletedFalse(accountId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByCategory(UUID categoryId, UUID userId) {
        Category category = categoryRepository.findByCategoryIdAndDeletedFalse(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (!category.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this category");
        }
        return transactionRepository.findByCategoryCategoryIdAndDeletedFalse(categoryId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransactionDTO updateTransaction(UUID transactionId, UUID userId, TransactionRequest request) {
        Transaction transaction = transactionRepository.findByTransactionIdAndDeletedFalse(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        verifyOwnership(transaction, userId);

        Account oldAccount = transaction.getAccount();

        // Reverse old balance effect
        if (transaction.getTransactionType() == TransactionType.INCOME) {
            oldAccount.setBalance(oldAccount.getBalance().subtract(transaction.getAmount()));
        } else {
            oldAccount.setBalance(oldAccount.getBalance().add(transaction.getAmount()));
        }

        TransactionType newType = TransactionType.valueOf(request.getTransactionType().toUpperCase());

        Account newAccount = accountRepository.findByAccountIdAndDeletedFalse(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (!newAccount.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this account");
        }

        // Apply new balance effect
        if (newType == TransactionType.INCOME) {
            newAccount.setBalance(newAccount.getBalance().add(request.getAmount()));
        } else {
            BigDecimal newBalance = newAccount.getBalance().subtract(request.getAmount());
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Insufficient balance. Available: " + newAccount.getBalance());
            }
            newAccount.setBalance(newBalance);
        }

        accountRepository.save(oldAccount);
        if (!oldAccount.getAccountId().equals(newAccount.getAccountId())) {
            accountRepository.save(newAccount);
        }

        transaction.setAccount(newAccount);
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionType(newType);

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findByCategoryIdAndDeletedFalse(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            transaction.setCategory(category);
        } else {
            transaction.setCategory(null);
        }

        // Handle tags
        if (request.getTagIds() != null) {
            Set<Tag> tags = request.getTagIds().isEmpty() 
                    ? new HashSet<>()
                    : tagRepository.findByTagIdInAndUserUserId(request.getTagIds(), userId);
            transaction.setTags(tags);
        }

        transaction = transactionRepository.save(transaction);
        log.info("Transaction updated: {}", transactionId);
        return toDTO(transaction);
    }

    @Override
    @Transactional
    public void deleteTransaction(UUID transactionId, UUID userId) {
        Transaction transaction = transactionRepository.findByTransactionIdAndDeletedFalse(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        verifyOwnership(transaction, userId);

        // Reverse balance effect
        Account account = transaction.getAccount();
        if (transaction.getTransactionType() == TransactionType.INCOME) {
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        } else {
            account.setBalance(account.getBalance().add(transaction.getAmount()));
        }
        accountRepository.save(account);

        transaction.softDelete();
        transactionRepository.save(transaction);
        log.info("Transaction soft-deleted: {}", transactionId);
    }

    @Override
    @Transactional
    public TransactionDTO restoreTransaction(UUID transactionId, UUID userId) {
        Transaction transaction = transactionRepository.findByTransactionIdAndDeletedTrue(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Deleted transaction not found"));
        verifyOwnership(transaction, userId);

        // Re-apply balance effect
        Account account = transaction.getAccount();
        if (transaction.getTransactionType() == TransactionType.INCOME) {
            account.setBalance(account.getBalance().add(transaction.getAmount()));
        } else {
            BigDecimal newBalance = account.getBalance().subtract(transaction.getAmount());
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Cannot restore: insufficient balance. Available: " + account.getBalance());
            }
            account.setBalance(newBalance);
        }
        accountRepository.save(account);

        transaction.restore();
        transaction = transactionRepository.save(transaction);
        log.info("Transaction restored: {}", transactionId);
        return toDTO(transaction);
    }

    private void verifyOwnership(Transaction transaction, UUID userId) {
        if (!transaction.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this transaction");
        }
    }

    @Override
    @Transactional
    public List<TransactionDTO> transfer(UUID userId, TransferRequest request) {
        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new BadRequestException("Source and destination accounts must be different");
        }

        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Account fromAccount = accountRepository.findByAccountIdAndDeletedFalse(request.getFromAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found"));
        if (!fromAccount.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to the source account");
        }

        Account toAccount = accountRepository.findByAccountIdAndDeletedFalse(request.getToAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found"));
        if (!toAccount.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to the destination account");
        }

        BigDecimal newFromBalance = fromAccount.getBalance().subtract(request.getAmount());
        if (newFromBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Insufficient balance. Available: " + fromAccount.getBalance());
        }

        String desc = request.getDescription() != null ? request.getDescription() : "Transfer";

        // Debit from source account
        Transaction debit = new Transaction();
        debit.setUser(user);
        debit.setAccount(fromAccount);
        debit.setAmount(request.getAmount());
        debit.setDescription(desc + " → " + toAccount.getAccountName());
        debit.setTransactionType(TransactionType.EXPENSE);
        debit.setDate(LocalDateTime.now());

        // Credit to destination account
        Transaction credit = new Transaction();
        credit.setUser(user);
        credit.setAccount(toAccount);
        credit.setAmount(request.getAmount());
        credit.setDescription(desc + " ← " + fromAccount.getAccountName());
        credit.setTransactionType(TransactionType.INCOME);
        credit.setDate(LocalDateTime.now());

        fromAccount.setBalance(newFromBalance);
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        debit = transactionRepository.save(debit);
        credit = transactionRepository.save(credit);

        log.info("Transfer of {} from account {} to account {} for user {}",
                request.getAmount(), request.getFromAccountId(), request.getToAccountId(), userId);

        return List.of(toDTO(debit), toDTO(credit));
    }

    private TransactionDTO toDTO(Transaction t) {
        List<TagDTO> tagDTOs = t.getTags() != null 
                ? t.getTags().stream()
                    .filter(tag -> !tag.isDeleted())
                    .map(tag -> new TagDTO(tag.getTagId(), tag.getName(), tag.getColor(), tag.getDescription(), 0))
                    .collect(Collectors.toList())
                : List.of();

        return new TransactionDTO(
                t.getTransactionId(),
                t.getAccount().getAccountId(),
                t.getAmount(),
                t.getDate(),
                t.getDescription(),
                t.getTransactionType().name(),
                t.getCategory() != null ? t.getCategory().getCategoryId() : null,
                t.getCategory() != null ? t.getCategory().getName() : null,
                tagDTOs,
                t.getCreatedAt()
        );
    }
}
