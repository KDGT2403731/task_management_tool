package com.example.taskmanagementtool.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Data;

@Entity
@Table(name = "integrations", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "service_name" }))
@Data
public class Integration {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "service_name", nullable = false, length = 100)
	private String serviceName;

	@Column(name = "api_endpoint", columnDefinition = "TEXT")
	private String apiEndpoint;

	@Column(name = "access_token_encrypted", columnDefinition = "TEXT")
	private String accessTokenEncrypted;

	@Column(name = "refresh_token_encrypted", columnDefinition = "TEXT")
	private String refreshTokenEncrypted;

	@Column(name = "expires_at")
	private LocalDateTime expiresAt;
}
