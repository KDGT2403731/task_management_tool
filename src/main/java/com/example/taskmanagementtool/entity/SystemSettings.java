package com.example.taskmanagementtool.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "system_settings")
@Data
public class SystemSettings {

	@Id
	private Long id = 1L; // シングルトン設定なので固定ID

	@Column(name = "maintenance_mode", nullable = false)
	private boolean maintenanceMode = false;

	@Column(name = "announcement_message", columnDefinition = "TEXT")
	private String announcementMessage;

	@Column(name = "backup_frequency", nullable = false)
	private String backupFrequency = "DAILY"; // DAILY, WEEKLY, MONTHLY

	@Column(name = "last_backup_at")
	private LocalDateTime lastBackupAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@ManyToOne
	@JoinColumn(name = "updated_by")
	private User updatedBy;
}