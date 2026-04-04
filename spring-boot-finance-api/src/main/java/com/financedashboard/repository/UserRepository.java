package com.financedashboard.repository;

import com.financedashboard.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.status = :status")
    List<User> findAllByStatus(@Param("status") User.UserStatus status);

    @Query("SELECT u FROM User u WHERE u.status != 'DELETED'")
    Page<User> findAllActiveUsers(Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.status = 'ACTIVE'")
    List<User> findAllByRoleName(@Param("roleName") String roleName);

    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND u.status != 'DELETED'")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void updateLastLoginTime(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);

    @Modifying
    @Query("UPDATE User u SET u.status = :status, u.lastModifiedBy = :modifiedBy WHERE u.id = :userId")
    void updateUserStatus(@Param("userId") Long userId, @Param("status") User.UserStatus status, @Param("modifiedBy") Long modifiedBy);

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 'ACTIVE'")
    long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.status = 'ACTIVE'")
    long countUsersByRole(@Param("roleName") String roleName);
}
