package com.example.taskmanagementtool.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taskmanagementtool.entity.Team;
import com.example.taskmanagementtool.entity.User;
import com.example.taskmanagementtool.repository.TeamRepository;
import com.example.taskmanagementtool.repository.UserRepository;

@Service
public class UserService {
	public static final List<String> VALID_ROLES = List.of("ADMIN", "MEMBER", "GUEST");

	private final UserRepository userRepository;
	private final TeamRepository teamRepository;

	public UserService(UserRepository userRepository, TeamRepository teamRepository) {
		this.userRepository = userRepository;
		this.teamRepository = teamRepository;
	}

	@Transactional(readOnly = true)
	public List<User> listAllUsers() {
		return userRepository.findAll();
	}

	@Transactional(readOnly = true)
	public User getUserById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("ユーザーが存在しません: " + id));
	}

	@Transactional(readOnly = true)
	public long countUsers() {
		return userRepository.count();
	}

	@Transactional
	public User updateRole(Long id, String role) {
		if (!VALID_ROLES.contains(role)) {
			throw new IllegalArgumentException("不正なロールです: " + role);
		}
		User user = getUserById(id);
		user.setRole(role);
		return userRepository.save(user);
	}

	@Transactional
	public User updateTeam(Long id, Long teamId) {
		User user = getUserById(id);
		if (teamId == null) {
			user.setTeam(null);
		} else {
			Team team = teamRepository.findById(teamId)
					.orElseThrow(() -> new IllegalArgumentException("チームが存在しません: " + teamId));
			user.setTeam(team);
		}
		return userRepository.save(user);
	}

	@Transactional
	public void deleteUser(Long id, String currentUserEmail) {
		User currentUser = userRepository.findByEmail(currentUserEmail)
				.orElseThrow(() -> new IllegalArgumentException("ユーザーが存在しません: " + currentUserEmail));

		if (currentUser.getId().equals(id)) {
			throw new IllegalStateException("自分自身のアカウントは削除できません。");
		}

		userRepository.deleteById(id);
	}
}
