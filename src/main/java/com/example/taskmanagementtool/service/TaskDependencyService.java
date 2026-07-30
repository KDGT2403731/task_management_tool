package com.example.taskmanagementtool.service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

		// 循環依存チェック：succeedingTaskId側から既存の依存関係を辿ってprecedingTaskIdに到達できる場合、
		// この依存関係を追加すると「AがBの前提かつBがAの前提」のような循環が発生してしまうため禁止する。
		// （例：?????→タスク2 が既にある状態で タスク2→????? を追加しようとするケースを防ぐ）
		if (hasPathBetween(succeedingTaskId, precedingTaskId)) {
			throw new IllegalArgumentException(
					"この組み合わせは既存の依存関係と矛盾するため登録できません（タスク同士が互いの前提になってしまいます）。");
		}

		TaskDependency dependency = new TaskDependency();
		dependency.setPrecedingTask(precedingTask);
		dependency.setSucceedingTask(succeedingTask);
		dependency.setDependencyType(dependencyType != null ? dependencyType : "FS");
		dependency.setLagDays(lagDays != null ? lagDays : 0);

		taskDependencyRepository.save(dependency);
	}

	/**
	 * 既存の依存関係（前提→後続の有向グラフ）だけを辿って、fromTaskIdからtoTaskIdに到達できるかを判定する。
	 * 到達できる場合、fromTaskIdを前提・toTaskIdを後続とする新規登録は循環依存を生む。
	 */
	private boolean hasPathBetween(Long fromTaskId, Long toTaskId) {
		Set<Long> visited = new HashSet<>();
		Deque<Long> stack = new ArrayDeque<>();
		stack.push(fromTaskId);

		while (!stack.isEmpty()) {
			Long current = stack.pop();
			if (current.equals(toTaskId)) {
				return true;
			}
			if (!visited.add(current)) {
				continue;
			}
			for (TaskDependency dep : taskDependencyRepository.findByPrecedingTaskId(current)) {
				stack.push(dep.getSucceedingTask().getId());
			}
		}
		return false;
	}

	@Transactional(readOnly = true)
	public List<TaskDependency> getDependenciesByProject(Long projectId) {
		return taskDependencyRepository.findByPrecedingTaskProjectId(projectId);
	}

	@Transactional(readOnly = true)
	public List<TaskDependency> getPrecedingDependencies(Long taskId) {
		return taskDependencyRepository.findBySucceedingTaskId(taskId);
	}

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