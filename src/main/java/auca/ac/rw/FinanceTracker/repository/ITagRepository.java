package auca.ac.rw.FinanceTracker.repository;

import auca.ac.rw.FinanceTracker.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ITagRepository extends JpaRepository<Tag, UUID> {

    List<Tag> findByUserUserIdAndDeletedFalse(UUID userId);

    Page<Tag> findByUserUserIdAndDeletedFalse(UUID userId, Pageable pageable);

    Optional<Tag> findByTagIdAndDeletedFalse(UUID tagId);

    Optional<Tag> findByNameAndUserUserIdAndDeletedFalse(String name, UUID userId);

    boolean existsByNameAndUserUserIdAndDeletedFalse(String name, UUID userId);

    @Query("SELECT t FROM Tag t WHERE t.tagId IN :tagIds AND t.user.userId = :userId AND t.deleted = false")
    Set<Tag> findByTagIdInAndUserUserId(@Param("tagIds") Set<UUID> tagIds, @Param("userId") UUID userId);

    @Query("SELECT t FROM Tag t JOIN t.transactions tr WHERE tr.transactionId = :transactionId AND t.deleted = false")
    Set<Tag> findByTransactionId(@Param("transactionId") UUID transactionId);
}
