package com.example.taskmanagementtool.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taskmanagementtool.entity.Team;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
	Optional<Team> findByName(String name);
}
