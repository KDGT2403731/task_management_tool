package com.example.taskmanagementtool.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taskmanagementtool.entity.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
	List<Project> findByTeamId(Long teamId);

	List<Project> findByOwnerId(Long ownerId);
}
