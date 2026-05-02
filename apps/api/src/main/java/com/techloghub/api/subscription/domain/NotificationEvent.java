package com.techloghub.api.subscription.domain;

import static com.techloghub.api.common.domain.DomainGuard.normalizeBlankToNull;
import static com.techloghub.api.common.domain.DomainGuard.requireNonNull;

import java.time.Instant;

import com.techloghub.api.common.domain.BaseEntity;
import com.techloghub.api.content.domain.ArchivedPost;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Getter
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "notification_event",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_notification_event_subscriber_post", columnNames = {"subscriber_id", "archived_post_id"})
	},
	indexes = {
		@Index(name = "idx_notification_event_status", columnList = "status")
	}
)
public class NotificationEvent extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "subscriber_id", nullable = false)
	private Subscriber subscriber;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "archived_post_id", nullable = false)
	private ArchivedPost archivedPost;

	@Enumerated(EnumType.STRING)
	@Column(name = "channel", nullable = false, length = 30)
	private NotificationChannel channel;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	private NotificationStatus status;

	@Column(name = "requested_at", nullable = false)
	private Instant requestedAt;

	@Column(name = "sent_at")
	private Instant sentAt;

	@Column(name = "failure_reason", columnDefinition = "text")
	private String failureReason;

	private NotificationEvent(
		Subscriber subscriber,
		ArchivedPost archivedPost,
		NotificationChannel channel,
		Instant requestedAt
	) {
		this.subscriber = requireNonNull(subscriber, "subscriber");
		this.archivedPost = requireNonNull(archivedPost, "archivedPost");
		this.channel = requireNonNull(channel, "channel");
		this.requestedAt = requireNonNull(requestedAt, "requestedAt");
		this.status = NotificationStatus.REQUESTED;
	}

	public static NotificationEvent requestEmail(Subscriber subscriber, ArchivedPost archivedPost, Instant requestedAt) {
		return new NotificationEvent(subscriber, archivedPost, NotificationChannel.EMAIL, requestedAt);
	}

	public void markSent(Instant sentAt) {
		this.sentAt = requireNonNull(sentAt, "sentAt");
		this.failureReason = null;
		this.status = NotificationStatus.SENT;
	}

	public void markFailed(String failureReason) {
		this.failureReason = normalizeBlankToNull(failureReason);
		this.status = NotificationStatus.FAILED;
	}

	public void markSkipped(String reason) {
		this.failureReason = normalizeBlankToNull(reason);
		this.status = NotificationStatus.SKIPPED;
	}

	public boolean isRequested() {
		return status == NotificationStatus.REQUESTED;
	}
}
