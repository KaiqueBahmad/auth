package kaiquebt.dev.auth.repository;

import kaiquebt.dev.auth.dto.SessionHistoryDto.ISessionHistoryDto;
import kaiquebt.dev.auth.model.BaseUser;
import kaiquebt.dev.auth.model.BaseUserSessionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

@NoRepositoryBean
public interface BaseUserSessionLogRepository<L extends BaseUserSessionLog<?>> extends JpaRepository<L, Long> {

    @Query("""
            SELECT
                log.id as id,
                log.sessionOrigin as origin,
                log.ipAddress as ipAddress,
                log.userAgent as userAgent,
                user.username as performedBy,
                log.createdAt as createdAt
            from #{#entityName} log
            LEFT JOIN log.performedBy user
            where log.user.id = :userId
            and (COALESCE(:startDate, log.createdAt) = log.createdAt OR log.createdAt >= :startDate)
            and (COALESCE(:endDate, log.createdAt) = log.createdAt OR log.createdAt <= :endDate)
            """)
    Page<ISessionHistoryDto> findAllProjected(
            Pageable pageable,
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // /**
    // * Find all session logs for a specific user ID
    // */
    // List<T> findByUserId(Long userId);

    // /**
    // * Find session logs for a specific user ID with pagination
    // */
    // Page<T> findByUserId(Long userId, Pageable pageable);

    // /**
    // * Find session logs by user ID and session origin
    // */
    // List<T> findByUserIdAndSessionOrigin(Long userId, UserSessionOrigin
    // sessionOrigin);

    // /**
    // * Find session logs by user ID within a date range
    // */
    // List<T> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startDate,
    // LocalDateTime endDate);

    // /**
    // * Find session logs by user ID and IP address
    // */
    // List<T> findByUserIdAndIpAddress(Long userId, String ipAddress);

    // /**
    // * Find session logs performed by a specific user
    // */
    // List<T> findByPerformedById(Long performedByUserId);

    // /**
    // * Count session logs for a specific user
    // */
    // Long countByUserId(Long userId);

    // /**
    // * Count session logs for a user within a date range
    // */
    // Long countByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startDate,
    // LocalDateTime endDate);

    // /**
    // * Find recent session logs for a user (configurable limit)
    // */
    // Page<T> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    // /**
    // * Find distinct IP addresses used by a user
    // */
    // @Query("SELECT DISTINCT log.ipAddress FROM #{#entityName} log WHERE
    // log.user.id = :userId AND log.ipAddress IS NOT NULL")
    // List<String> findDistinctIpAddressesByUserId(@Param("userId") Long userId);

    // /**
    // * Find session logs with suspicious activity (multiple IPs in short time)
    // */
    // @Query("""
    // SELECT log FROM #{#entityName} log
    // WHERE log.user.id = :userId
    // AND log.createdAt >= :timeThreshold
    // GROUP BY log.ipAddress
    // HAVING COUNT(DISTINCT log.ipAddress) > 1
    // """)
    // List<T> findSuspiciousActivityByUserId(@Param("userId") Long userId,
    // @Param("timeThreshold") LocalDateTime timeThreshold);
}