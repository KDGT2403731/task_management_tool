package com.example.taskmanagementtool.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taskmanagementtool.entity.Milestone;
import com.example.taskmanagementtool.entity.Project;
import com.example.taskmanagementtool.repository.MilestoneRepository;
import com.example.taskmanagementtool.repository.ProjectRepository;

@Service
public class MilestoneService {
	public static final List<String> VALID_STATUSES = List.of("OPEN", "ACHIEVED", "MISSED");

	private final MilestoneRepository milestoneRepository;
	private final ProjectRepository projectRepository;

	public MilestoneService(MilestoneRepository milestoneRepository, ProjectRepository projectRepository) {
		this.milestoneRepository = milestoneRepository;
		this.projectRepository = projectRepository;
	}

	@Transactional(readOnly = true)
	public List<Milestone> listByProject(Long projectId) {
		ensureProjectExists(projectId);
		return milestoneRepository.findByProjectIdOrderByTargetDateAsc(projectId);
	}

	@Transactional(readOnly = true)
	public Milestone getMilestoneInProject(Long projectId, Long milestoneId) {
		return findMilestoneInProject(projectId, milestoneId);
	}

	@Transactional
	public Milestone createMilestone(Long projectId, String title, LocalDate targetDate, String description) {
		Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new IllegalArgumentException("プロジェクトが存在しません: " + projectId));
		Milestone milestone = new Milestone();
		milestone.setProject(project);
		milestone.setTitle(title);
		milestone.setTargetDate(targetDate);
		milestone.setDescription(description);
		milestone.setStatus("OPEN"); // 新規作成時は常にOPEN

		return milestoneRepository.save(milestone);
	}

	@Transactional
	public Milestone updateMilestone(Long projectId, Long milestoneId, String title, LocalDate targetDate,
			String description) {
		Milestone milestone = findMilestoneInProject(projectId, milestoneId);
		milestone.setTitle(title);
		milestone.setTargetDate(targetDate);
		milestone.setDescription(description);
		return milestoneRepository.save(milestone);
	}

	@Transactional
	public Milestone updateStatus(Long projectId, Long milestoneId, String status) {
		if (!VALID_STATUSES.contains(status)) {
			throw new IllegalArgumentException("不正なステータスです。" + status);
		}
		Milestone milestone = findMilestoneInProject(projectId, milestoneId);
		milestone.setStatus(status);
		return milestoneRepository.save(milestone);
	}

	@Transactional
	public void deleteMilestone(Long projectId, Long milestoneId) {
		Milestone milestone = findMilestoneInProject(projectId, milestoneId);
		if (milestone.getTasks() != null && !milestone.getTasks().isEmpty()) {
			throw new IllegalStateException(
					"このマイルストーンには" + milestone.getTasks().size() + "件のタスクが紐づいているため削除できません。先にタスクの紐付けを解除してください。");
		}
		milestoneRepository.deleteById(milestoneId);
	}

	private void ensureProjectExists(Long projectId) {
		if (!projectRepository.existsById(projectId)) {
			throw new IllegalArgumentException("プロジェクトが存在しません: " + projectId);
		}
	}

	private Milestone findMilestoneInProject(Long projectId, Long milestoneId) {
		Milestone milestone = milestoneRepository.findById(milestoneId)
				.orElseThrow(() -> new IllegalArgumentException("マイルストーンが存在しません: " + milestoneId));

		if (milestone.getProject() == null || !projectId.equals(milestone.getProject().getId())) {
			throw new IllegalArgumentException(
					"指定されたプロジェクトにこのマイルストーンは属していません: projectId=" + projectId + ", milestoneId=" + milestoneId);
		}

		return milestone;
	}

	@Transactional(readOnly = true)
	public List<Milestone> getUpcomingMilestonesForUser(String username) {
		if (username == null || username.isBlank()) {
			return List.of();
		}
		return milestoneRepository.findUpcomingMilestonesByUsername(username, LocalDate.now());
	}
}
