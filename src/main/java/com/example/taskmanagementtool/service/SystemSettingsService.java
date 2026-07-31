package com.example.taskmanagementtool.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taskmanagementtool.entity.SystemSettings;
import com.example.taskmanagementtool.entity.User;
import com.example.taskmanagementtool.repository.SystemSettingsRepository;

@Service
@Transactional
public class SystemSettingsService {

	private static final Long SETTINGS_ID = 1L;
	private static final List<String> VALID_FREQUENCIES = List.of("DAILY", "WEEKLY", "MONTHLY");

	private final SystemSettingsRepository systemSettingsRepository;

	public SystemSettingsService(SystemSettingsRepository systemSettingsRepository) {
		this.systemSettingsRepository = systemSettingsRepository;
	}

	public SystemSettings getSettings() {
		return systemSettingsRepository.findById(SETTINGS_ID)
				.orElseGet(() -> {
					SystemSettings settings = new SystemSettings();
					settings.setId(SETTINGS_ID);
					settings.setUpdatedAt(LocalDateTime.now());
					return systemSettingsRepository.save(settings);
				});
	}

	public void updateSettings(boolean maintenanceMode, String announcementMessage,
			String backupFrequency, User updatedBy) {
		if (!VALID_FREQUENCIES.contains(backupFrequency)) {
			throw new IllegalArgumentException("不正なバックアップ頻度です: " + backupFrequency);
		}
		SystemSettings settings = getSettings();
		settings.setMaintenanceMode(maintenanceMode);
		settings.setAnnouncementMessage(announcementMessage);
		settings.setBackupFrequency(backupFrequency);
		settings.setUpdatedAt(LocalDateTime.now());
		settings.setUpdatedBy(updatedBy);
		systemSettingsRepository.save(settings);
	}

	public void runBackupNow() {
		SystemSettings settings = getSettings();
		settings.setLastBackupAt(LocalDateTime.now());
		systemSettingsRepository.save(settings);
	}
}