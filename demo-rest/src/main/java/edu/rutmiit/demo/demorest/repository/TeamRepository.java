package edu.rutmiit.demo.demorest.repository;

import edu.rutmiit.demo.demorest.entity.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<TeamEntity, UUID> {

    @Query("select t from TeamEntity t where t.id = :id")
    Optional<TeamEntity> findByPublicId(@Param("id") Long id);

    boolean existsByName(String name);

    @Query(value = "SELECT nextval('team_id_seq')", nativeQuery = true)
    Long nextTeamId();
}