package com.example.taskmanagementtool.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taskmanagementtool.entity.Integration;
import com.example.taskmanagementtool.entity.User;
import com.example.taskmanagementtool.repository.IntegrationRepository;
import com.example.taskmanagementtool.repository.UserRepository;

@Service
public class IntegrationService {

	public static final List<String> VALID_SERVICES = List.of("SLACK", "TEAMS", "GOOGLE_CALENDAR");

	private final IntegrationRepository integrationRepository;
	private final UserRepository userRepository;
	private final TokenEncryptionService tokenEncryptionService;

	public IntegrationService(IntegrationRepository integrationRepository, UserRepository userRepository,
			TokenEncryptionService tokenEncryptionService) {
		this.integrationRepository = integrationRepository;
		this.userRepository = userRepository;
		this.tokenEncryptionService = tokenEncryptionService;
	}

	@Transactional(readOnly = true)
	public List<Integration> listForUser(String email) {
		User user = findUserByEmail(email);
		return integrationRepository.findByUserId(user.getId());
	}

	@Transactional
	public Integration connect(String email, String serviceName, String apiEndpoint, String accessToken,
			String refreshToken, LocalDateTime expiresAt) {
		if (!VALID_SERVICES.contains(serviceName)) {
			throw new IllegalArgumentException("不正なアクセス連携です: " + serviceName);
		}
		User user = findUserByEmail(email);
		Integration integration = integrationRepository.findByUserIdAndServiceName(user.getId(), serviceName)
				.orElseGet(Integration::new);

		integration.setUser(user);
		integration.setServiceName(serviceName);
		integration.setApiEndpoint(apiEndpoint);
		integration.setAccessTokenEncrypted(tokenEncryptionService.encrypt(accessToken));
		integration.setRefreshTokenEncrypted(tokenEncryptionService.encrypt(refreshToken));
		integration.setExpiresAt(expiresAt);

		return integrationRepository.save(integration);
	}

	@Transactional
	public void disconnect(String email, Long integrationId) {
		User user = findUserByEmail(email);
		Integration integration = integrationRepository.findById(integrationId)
				.orElseThrow(() -> new IllegalArgumentException("連携情報が存在しません: " + integrationId));

		if (integration.getUser() == null || !integration.getUser().getId().equals(user.getId())) {
			throw new IllegalArgumentException("この連携情報を操作する権限がありません。");
		}

		integrationRepository.deleteById(integrationId);
	}

	private User findUserByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("ユーザーが存在しません: " + email));
	}
}
