package com.techloghub.api.subscription.domain;

import static com.techloghub.api.common.domain.DomainGuard.requireNonBlank;
import static com.techloghub.api.common.domain.DomainGuard.requireNonNull;

import java.time.Instant;

import com.techloghub.api.common.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
	name = "subscription_verification_request",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_subscription_verification_token_hash", columnNames = "token_hash")
	},
	indexes = {
		@Index(name = "idx_subscription_verification_subscriber_id", columnList = "subscriber_id")
	}
)
public class SubscriptionVerificationRequest extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "subscriber_id", nullable = false)
	private Subscriber subscriber;

	@Column(name = "token_hash", nullable = false, length = 128)
	private String tokenHash;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "used_at")
	private Instant usedAt;

	protected SubscriptionVerificationRequest() {
	}

	private SubscriptionVerificationRequest(Subscriber subscriber, String tokenHash, Instant expiresAt) {
		this.subscriber = requireNonNull(subscriber, "subscriber");
		this.tokenHash = requireNonBlank(tokenHash, "tokenHash");
		this.expiresAt = requireNonNull(expiresAt, "expiresAt");
	}

	public static SubscriptionVerificationRequest issue(Subscriber subscriber, String tokenHash, Instant expiresAt) {
		return new SubscriptionVerificationRequest(subscriber, tokenHash, expiresAt);
	}

	public boolean isExpired(Instant now) {
		return expiresAt.isBefore(requireNonNull(now, "now"));
	}

	public boolean isUsed() {
		return usedAt != null;
	}

	public void markUsed(Instant usedAt) {
		if (isUsed()) {
			throw new IllegalStateException("verification request already used");
		}
		this.usedAt = requireNonNull(usedAt, "usedAt");
	}

	public Long getId() {
		return id;
	}

	public Subscriber getSubscriber() {
		return subscriber;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getUsedAt() {
		return usedAt;
	}
}
