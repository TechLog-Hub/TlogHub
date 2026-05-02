package com.techloghub.api.subscription.domain;

import static com.techloghub.api.common.domain.DomainGuard.normalizeBlankToNull;
import static com.techloghub.api.common.domain.DomainGuard.requireNonBlank;

import java.time.Instant;

import com.techloghub.api.common.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
	name = "subscriber",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_subscriber_email", columnNames = "email"),
		@UniqueConstraint(name = "uk_subscriber_manage_token_hash", columnNames = "manage_token_hash")
	}
)
public class Subscriber extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "email", nullable = false, length = 320)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 40)
	private SubscriberStatus status;

	@Column(name = "manage_token_hash", length = 128)
	private String manageTokenHash;

	@Column(name = "verified_at")
	private Instant verifiedAt;

	protected Subscriber() {
	}

	private Subscriber(String email) {
		this.email = requireNonBlank(email, "email").toLowerCase();
		this.status = SubscriberStatus.PENDING_VERIFICATION;
	}

	public static Subscriber pending(String email) {
		return new Subscriber(email);
	}

	public void activate(String manageTokenHash, Instant verifiedAt) {
		this.status = SubscriberStatus.ACTIVE;
		this.manageTokenHash = requireNonBlank(manageTokenHash, "manageTokenHash");
		this.verifiedAt = verifiedAt == null ? Instant.now() : verifiedAt;
	}

	public void unsubscribe() {
		this.status = SubscriberStatus.UNSUBSCRIBED;
	}

	public void markBounced() {
		this.status = SubscriberStatus.BOUNCED;
	}

	public void resetVerification() {
		this.status = SubscriberStatus.PENDING_VERIFICATION;
		this.manageTokenHash = null;
		this.verifiedAt = null;
	}

	public void rotateManageToken(String manageTokenHash) {
		this.manageTokenHash = normalizeBlankToNull(manageTokenHash);
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public SubscriberStatus getStatus() {
		return status;
	}

	public String getManageTokenHash() {
		return manageTokenHash;
	}

	public Instant getVerifiedAt() {
		return verifiedAt;
	}
}
