package auca.ac.rw.FinanceTracker.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import auca.ac.rw.FinanceTracker.model.Account;

@Repository
public interface IAccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findByUserUserIdAndDeletedFalse(UUID userId);

    Page<Account> findByUserUserIdAndDeletedFalse(UUID userId, Pageable pageable);

    Optional<Account> findByAccountIdAndDeletedFalse(UUID accountId);

    Optional<Account> findByAccountIdAndDeletedTrue(UUID accountId);
}
