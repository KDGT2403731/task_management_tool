package com.example.taskmanagementtool.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.taskmanagementtool.entity.Milestone;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
	List<Milestone> findByProjectIdOrderByTargetDateAsc(Long projectId);

	@Query("SELECT m FROM Milestone m " +
			"JOIN m.project p " +
			"JOIN p.members pm " +
			"WHERE pm.email = :username " +
			"AND m.status = 'OPEN' " +
			"AND m.targetDate >= :today " +
			"ORDER BY m.targetDate ASC")
	List<Milestone> findUpcomingMilestonesByUsername(
			@Param("username") String username,
			@Param("today") LocalDate today);
}
