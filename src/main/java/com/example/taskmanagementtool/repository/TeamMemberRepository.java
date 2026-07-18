package com.example.taskmanagementtool.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taskmanagementtool.entity.TeamMember;
import com.example.taskmanagementtool.entity.TeamMemberId;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, TeamMemberId> {
	List<TeamMember> findByTeamId(Long teamId);

	List<TeamMember> findByUserId(Long userId);
}
