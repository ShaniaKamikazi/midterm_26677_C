package auca.ac.rw.FinanceTracker.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import auca.ac.rw.FinanceTracker.model.Category;

@Repository
public interface ICategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findByNameAndUserUserIdAndDeletedFalse(String name, UUID userId);

    boolean existsByNameAndUserUserIdAndDeletedFalse(String name, UUID userId);

    List<Category> findByUserUserIdAndDeletedFalse(UUID userId);

    Page<Category> findByUserUserIdAndDeletedFalse(UUID userId, Pageable pageable);

    Optional<Category> findByCategoryIdAndDeletedFalse(UUID categoryId);

    @Query("SELECT c FROM Category c WHERE c.user.userId = :userId AND c.deleted = false AND (" +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Category> searchCategories(@Param("userId") UUID userId, @Param("searchTerm") String searchTerm);

    Optional<Category> findByCategoryIdAndDeletedTrue(UUID categoryId);
}

