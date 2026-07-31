package com.example.taskmanagementtool.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taskmanagementtool.entity.Project;
import com.example.taskmanagementtool.entity.User;
import com.example.taskmanagementtool.repository.ProjectRepository;
import com.example.taskmanagementtool.repository.RecurringRuleRepository;
import com.example.taskmanagementtool.repository.TaskDependencyRepository;
import com.example.taskmanagementtool.repository.UserRepository;

@Service
public class ProjectService {

	private final ProjectRepository projectRepository;
	private final UserRepository userRepository;
	private final RecurringRuleRepository recurringRuleRepository;
	private final TaskDependencyRepository taskDependencyRepository;

	public ProjectService(ProjectRepository projectRepository, UserRepository userRepository,
			RecurringRuleRepository recurringRuleRepository, TaskDependencyRepository taskDependencyRepository) {
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
		this.recurringRuleRepository = recurringRuleRepository;
		this.taskDependencyRepository = taskDependencyRepository;
	}

	@Transactional(readOnly = true)
	public List<Project> listProjectsForUser(String email) {
		User currentUser = findUserByEmail(email);
		return currentUser.getProjects() == null ? List.of() : currentUser.getProjects().stream().toList();
	}

	@Transactional(readOnly = true)
	public Project getProjectById(Long projectId) {
		return projectRepository.findById(projectId)
				.orElseThrow(() -> new IllegalArgumentException("プロジェクトが存在しません: " + projectId));
	}

	/**
	 * ステータスが進行中(IN_PROGRESS)のプロジェクト数。管理者ダッシュボードのサマリー表示用。
	 */
	@Transactional(readOnly = true)
	public long countActiveProjects() {
		return projectRepository.countByStatus("IN_PROGRESS");
	}

	/**
	 * 指定ユーザーがこのプロジェクトを閲覧・操作できるかを検証する。
	 * ADMINは全プロジェクトにアクセス可能。それ以外はプロジェクトと同じチームに
	 * 所属している場合のみアクセス可能（URLのIDを変えて他チームのプロジェクトを
	 * 覗き見・改ざんできてしまうのを防ぐため）。
	 */
	@Transactional(readOnly = true)
	public void assertAccessible(Long projectId, String email) {
		Project project = getProjectById(projectId);
		User currentUser = findUserByEmail(email);

		if ("ADMIN".equals(currentUser.getRole())) {
			return;
		}

		boolean sameTeam = project.getTeam() != null
				&& currentUser.getTeam() != null
				&& project.getTeam().getId().equals(currentUser.getTeam().getId());

		if (!sameTeam) {
			throw new AccessDeniedException("このプロジェクトへのアクセス権がありません。");
		}
	}

	@Transactional
	public Project createProject(String email, String name, String description, LocalDate startDate,
			LocalDate endDate) {
		User currentUser = findUserByEmail(email);

		if (currentUser.getTeam() == null) {
			throw new IllegalStateException("チームに所属していないユーザーはプロジェクトを作成できません。先にチームへの参加が必要です。");
		}

		Project project = new Project();
		project.setName(name);
		project.setDescription(description);
		project.setStartDate(startDate);
		project.setEndDate(endDate);
		project.setStatus("NOT_STARTED");
		project.setOwner(currentUser);
		project.setTeam(currentUser.getTeam());

		projectRepository.save(project);

		if (currentUser.getProjects() == null) {
			currentUser.setProjects(new HashSet<>());
		}
		currentUser.getProjects().add(project);
		userRepository.save(currentUser);

		return project;
	}

	@Transactional
	public Project updateProject(Long projectId, String name, String description, LocalDate startDate,
			LocalDate endDate) {
		Project project = getProjectById(projectId);
		project.setName(name);
		project.setDescription(description);
		project.setStartDate(startDate);
		project.setEndDate(endDate);
		return projectRepository.save(project);
	}

	@Transactional
	public void deleteProject(Long projectId) {
		Project project = getProjectById(projectId);

		int recurringRuleCount = recurringRuleRepository.findByProjectId(projectId).size();
		if (recurringRuleCount > 0) {
			throw new IllegalStateException(
					"このプロジェクトには" + recurringRuleCount + "件の繰り返しルールが紐づいているため削除できません。先に繰り返し設定を削除してください。");
		}

		int dependencyCount = taskDependencyRepository.findByPrecedingTaskProjectId(projectId).size();
		if (dependencyCount > 0) {
			throw new IllegalStateException(
					"このプロジェクトには" + dependencyCount + "件のタスク依存関係が設定されているため削除できません。先に依存関係を解除してください。");
		}

		projectRepository.delete(project);
	}

	@Transactional
	public void addMember(Long projectId, Long userId) {
		Project project = getProjectById(projectId);
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("ユーザーが存在しません: " + userId));

		if (user.getTeam() == null || project.getTeam() == null
				|| !user.getTeam().getId().equals(project.getTeam().getId())) {
			throw new IllegalArgumentException("このユーザーはプロジェクトのチームに所属していないため追加できません。");
		}

		if (user.getProjects() == null) {
			user.setProjects(new HashSet<>());
		}
		user.getProjects().add(project);
		userRepository.save(user);
	}

	@Transactional
	public void removeMember(Long projectId, Long userId) {
		Project project = getProjectById(projectId);
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("ユーザーが存在しません: " + userId));

		if (project.getOwner() != null && project.getOwner().getId().equals(userId)) {
			throw new IllegalStateException("プロジェクトのオーナーはメンバーから外せません。");
		}

		if (user.getProjects() != null) {
			user.getProjects().remove(project);
			userRepository.save(user);
		}
	}

	private User findUserByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("ユーザーが存在しません: " + email));
	}
}