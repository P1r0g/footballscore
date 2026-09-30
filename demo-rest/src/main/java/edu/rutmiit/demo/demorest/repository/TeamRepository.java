package edu.rutmiit.demo.demorest.repository;

import edu.rutmiit.demo.demorest.entity.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<TeamEntity, Long> {
}