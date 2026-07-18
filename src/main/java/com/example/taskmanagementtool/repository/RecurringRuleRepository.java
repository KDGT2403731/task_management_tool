package com.example.taskmanagementtool.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taskmanagementtool.entity.RecurringRule;

@Repository
public interface RecurringRuleRepository extends JpaRepository<RecurringRule, Long> {
	List<RecurringRule> findByIsActiveTrue();

	List<RecurringRule> findByProjectId(Long projectId);
}
