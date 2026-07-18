package com.example.taskmanagementtool.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taskmanagementtool.entity.Team;
import com.example.taskmanagementtool.entity.User;
import com.example.taskmanagementtool.repository.TeamRepository;
import com.example.taskmanagementtool.repository.UserRepository;

@Service
public class TeamService {
	private final TeamRepository teamRepository;
	private final UserRepository userRepository;

	public TeamService(TeamRepository teamRepository, UserRepository userRepository) {
		this.teamRepository = teamRepository;
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public List<Team> listAllTeams() {
		return teamRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Team getTeamById(Long id) {
		return teamRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("チームが存在しません: " + id));
	}

	@Transactional(readOnly = true)
	public long countTeams() {
		return teamRepository.count();
	}

	@Transactional
	public Team createTeam(String name) {
		Team team = new Team();
		team.setName(name);
		return teamRepository.save(team);
	}

	@Transactional(readOnly = true)
	public List<User> findTeamMembers(Long teamId) {
		return userRepository.findAll().stream()
				.filter(u -> u.getTeam() != null && teamId.equals(u.getTeam().getId()))
				.toList();
	}

	@Transactional
	public void deleteTeam(Long id) {
		Team team = getTeamById(id);

		List<User> members = findTeamMembers(id);
		int projectCount = team.getProjects() == null ? 0 : team.getProjects().size();

		if (!members.isEmpty() || projectCount > 0) {
			throw new IllegalStateException(
					"このチームには" + members.size() + "人のメンバーと" + projectCount
							+ "件のプロジェクトが紐づいているため削除できません。先にメンバーの異動・プロジェクトの移管を行ってください。");
		}

		teamRepository.deleteById(id);
	}
}
