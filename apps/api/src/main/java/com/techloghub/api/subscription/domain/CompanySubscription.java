package com.techloghub.api.subscription.domain;

import static com.techloghub.api.common.domain.DomainGuard.requireNonNull;

import com.techloghub.api.common.domain.BaseEntity;
import com.techloghub.api.content.domain.Company;

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
	name = "company_subscription",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_company_subscription_subscriber_company", columnNames = {"subscriber_id", "company_id"})
	},
	indexes = {
		@Index(name = "idx_company_subscription_company_active", columnList = "company_id, active")
	}
)
public class CompanySubscription extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "subscriber_id", nullable = false)
	private Subscriber subscriber;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@Column(name = "active", nullable = false)
	private boolean active;

	protected CompanySubscription() {
	}

	private CompanySubscription(Subscriber subscriber, Company company) {
		this.subscriber = requireNonNull(subscriber, "subscriber");
		this.company = requireNonNull(company, "company");
		this.active = true;
	}

	public static CompanySubscription subscribe(Subscriber subscriber, Company company) {
		return new CompanySubscription(subscriber, company);
	}

	public void activate() {
		this.active = true;
	}

	public void unsubscribe() {
		this.active = false;
	}

	public Long getId() {
		return id;
	}

	public Subscriber getSubscriber() {
		return subscriber;
	}

	public Company getCompany() {
		return company;
	}

	public boolean isActive() {
		return active;
	}
}
