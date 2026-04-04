package com.financedashboard.repository;

import com.financedashboard.entity.RefreshToken;
import com.financedashboard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user")
    void revokeAllUserTokens(@Param("user") User user);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId")
    void revokeAllUserTokensById(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now OR rt.revoked = true")
    void deleteExpiredAndRevokedTokens(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revoked = false AND rt.expiresAt > :now")
    long countActiveTokensByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
