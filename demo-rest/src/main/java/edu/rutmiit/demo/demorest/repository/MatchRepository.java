package edu.rutmiit.demo.demorest.repository;

import edu.rutmiit.demo.demorest.entity.MatchEntity;
import edu.rutmiit.demo.footballscoreapicontract.dto.MatchStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MatchRepository extends JpaRepository<MatchEntity, Long> {

    @Query("""
            select m
            from MatchEntity m
            where (:teamId is null
                or m.homeTeam.id = :teamId
                or m.awayTeam.id = :teamId)
              and (:status is null or m.status = :status)
            """)
    Page<MatchEntity> findWithFilters(
            @Param("teamId") Long teamId,
            @Param("status") MatchStatus status,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select m
            from MatchEntity m
            where m.id = :id
            """)
    Optional<MatchEntity> findByIdForUpdate(@Param("id") Long id);

    @Modifying(flushAutomatically = true)
    @Query("""
            delete from MatchEntity m
            where m.homeTeam.id = :teamId
               or m.awayTeam.id = :teamId
            """)
    int deleteByTeamId(@Param("teamId") Long teamId);
}