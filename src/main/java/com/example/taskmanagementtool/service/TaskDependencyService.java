package com.example.taskmanagementtool.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taskmanagementtool.entity.Task;
import com.example.taskmanagementtool.entity.TaskDependency;
import com.example.taskmanagementtool.entity.TaskDependencyId;
import com.example.taskmanagementtool.repository.TaskDependencyRepository;
import com.example.taskmanagementtool.repository.TaskRepository;

@Service
public class TaskDependencyService {
	private final TaskDependencyRepository taskDependencyRepository;
	private final TaskRepository taskRepository;

	public TaskDependencyService(TaskDependencyRepository taskDependencyRepository, TaskRepository taskRepository) {
		this.taskDependencyRepository = taskDependencyRepository;
		this.taskRepository = taskRepository;
	}

	@Transactional
	public void addDependency(Long precedingTaskId, Long succeedingTaskId, String dependencyType, Integer lagDays) {
		if (precedingTaskId.equals(succeedingTaskId)) {
			throw new IllegalArgumentException("同じタスク同士を依存関係にすることはできません。");
		}

		Task precedingTask = taskRepository.findById(precedingTaskId)
				.orElseThrow(() -> new IllegalArgumentException("前提タスクが見つかりません: " + precedingTaskId));
		Task succeedingTask = taskRepository.findById(succeedingTaskId)
				.orElseThrow(() -> new IllegalArgumentException("後続タスクが見つかりません: " + succeedingTaskId));

		// 前提タスクと後続タスクが別プロジェクトだと、プロジェクト単位の依存関係一覧で不整合が起きるため禁止する
		Long precedingProjectId = precedingTask.getProject() != null ? precedingTask.getProject().getId() : null;
		Long succeedingProjectId = succeedingTask.getProject() != null ? succeedingTask.getProject().getId() : null;
		if (precedingProjectId == null || !precedingProjectId.equals(succeedingProjectId)) {
			throw new IllegalArgumentException("異なるプロジェクトのタスク同士を依存関係にすることはできません。");
		}

		TaskDependency dependency = new TaskDependency();
		dependency.setPrecedingTask(precedingTask);
		dependency.setSucceedingTask(succeedingTask);
		dependency.setDependencyType(dependencyType != null ? dependencyType : "FS");
		dependency.setLagDays(lagDays != null ? lagDays : 0);

		taskDependencyRepository.save(dependency);
	}

	@Transactional(readOnly = true)
	public List<TaskDependency> getDependenciesByProject(Long projectId) {
		return taskDependencyRepository.findByPrecedingTaskProjectId(projectId);
	}

	@Transactional(readOnly = true)
	public List<TaskDependency> getPrecedingDependencies(Long taskId) {
		return taskDependencyRepository.findBySucceedingTaskId(taskId);
	}

	/**
	 * 指定したタスクに依存している後続タスク一覧を取得する。
	 */
	@Transactional(readOnly = true)
	public List<TaskDependency> getSucceedingDependencies(Long taskId) {
		return taskDependencyRepository.findByPrecedingTaskId(taskId);
	}

	@Transactional
	public void removeDependency(Long precedingTaskId, Long succeedingTaskId) {
		TaskDependencyId id = new TaskDependencyId();
		id.setPrecedingTask(precedingTaskId);
		id.setSucceedingTask(succeedingTaskId);

		if (!taskDependencyRepository.existsById(id)) {
			throw new IllegalArgumentException("依存関係が存在しません。");
		}

		taskDependencyRepository.deleteById(id);
	}

	@Transactional(readOnly = true)
	public boolean isPrecedingTasksCompleted(Long succeedingTaskId) {
		List<TaskDependency> dependencies = taskDependencyRepository.findBySucceedingTaskId(succeedingTaskId);
		for (TaskDependency dep : dependencies) {
			if (!"DONE".equals(dep.getPrecedingTask().getStatus())) {
				return false; // 1つでも完了していない前提タスクがあればNG
			}
		}
		return true;
	}
}