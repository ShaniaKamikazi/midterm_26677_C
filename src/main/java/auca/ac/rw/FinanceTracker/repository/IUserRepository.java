package auca.ac.rw.FinanceTracker.repository;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import auca.ac.rw.FinanceTracker.model.User;

@Repository
public interface IUserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUserNameAndUserEmailAndDeletedFalse(String name, String email);

    Optional<User> findByUserNameAndDeletedFalse(String userName);

    boolean existsByUserEmailAndDeletedFalse(String email);

    boolean existsByUserNameAndDeletedFalse(String userName);

    Optional<User> findByUserEmailAndDeletedFalse(String email);

    Optional<User> findByUserEmail(String email);

    @Query("SELECT u FROM User u WHERE u.deleted = false AND (LOWER(u.userName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.userEmail) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);

    @Query("SELECT u FROM User u WHERE u.deleted = false")
    List<User> findAllActive();

    Optional<User> findByPasswordResetToken(String token);

    @Query("SELECT u FROM User u " +
           "JOIN u.village v " +
           "JOIN v.parent cell " +
           "JOIN cell.parent sector " +
           "JOIN sector.parent district " +
           "JOIN district.parent province " +
           "WHERE u.deleted = false " +
           "AND province.locationType = auca.ac.rw.FinanceTracker.enums.LocationType.PROVINCE " +
           "AND (LOWER(province.code) = LOWER(:identifier) OR LOWER(province.name) = LOWER(:identifier))")
    List<User> findUsersByProvinceCodeOrName(@Param("identifier") String identifier);
}
