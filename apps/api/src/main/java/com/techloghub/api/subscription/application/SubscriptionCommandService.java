package com.techloghub.api.subscription.application;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techloghub.api.common.error.BusinessException;
import com.techloghub.api.common.error.CommonErrorCode;
import com.techloghub.api.common.util.CollectionSupport;
import com.techloghub.api.common.util.CryptoSupport;
import com.techloghub.api.common.util.MaskingSupport;
import com.techloghub.api.common.util.StringNormalizer;
import com.techloghub.api.content.domain.Company;
import com.techloghub.api.content.domain.CompanyStatus;
import com.techloghub.api.content.repository.CompanyRepository;
import com.techloghub.api.subscription.domain.CompanySubscription;
import com.techloghub.api.subscription.domain.Subscriber;
import com.techloghub.api.subscription.domain.SubscriberStatus;
import com.techloghub.api.subscription.domain.SubscriptionVerificationRequest;
import com.techloghub.api.subscription.dto.SubscriptionManageResponse;
import com.techloghub.api.subscription.dto.SubscriptionRequestResponse;
import com.techloghub.api.subscription.dto.SubscriptionVerifyResponse;
import com.techloghub.api.subscription.error.SubscriptionErrorCode;
import com.techloghub.api.subscription.repository.CompanySubscriptionRepository;
import com.techloghub.api.subscription.repository.SubscriberRepository;
import com.techloghub.api.subscription.repository.SubscriptionVerificationRequestRepository;

import lombok.RequiredArgsConstructor;

/**
 * 이메일 기반 구독 생성, 확인, 관리 변경을 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionCommandService {

	private static final int MAX_COMPANY_COUNT = 20;

	private final SubscriberRepository subscriberRepository;
	private final SubscriptionVerificationRequestRepository verificationRequestRepository;
	private final CompanySubscriptionRepository companySubscriptionRepository;
	private final CompanyRepository companyRepository;
	private final SecureTokenGenerator secureTokenGenerator;

	@Value("${techlog.subscription.verification-token-ttl:PT24H}")
	private Duration verificationTokenTtl;

	@Value("${techlog.subscription.expose-dev-tokens:false}")
	private boolean exposeDevTokens;

	public SubscriptionRequestResponse request(String email, List<String> companySlugs) {
		String normalizedEmail = normalizeEmail(email);
		List<String> normalizedCompanySlugs = normalizeCompanySlugs(companySlugs);
		List<Company> companies = findActiveCompanies(normalizedCompanySlugs);
		Subscriber subscriber = findOrCreateSubscriber(normalizedEmail);
		if (subscriber.getStatus() == SubscriberStatus.BOUNCED) {
			throw new BusinessException(SubscriptionErrorCode.SUBSCRIPTION_NOT_ACTIVE);
		}
		if (subscriber.getStatus() == SubscriberStatus.UNSUBSCRIBED) {
			subscriber.resetVerification();
		}

		replacePendingSubscriptions(subscriber, companies);
		invalidatePreviousVerificationRequests(subscriber);

		String verificationToken = secureTokenGenerator.generate();
		Instant now = Instant.now();
		verificationRequestRepository.save(SubscriptionVerificationRequest.issue(
			subscriber,
			tokenHash(verificationToken),
			now.plus(verificationTokenTtl)
		));

		return SubscriptionRequestResponse.accepted(
			MaskingSupport.maskEmail(subscriber.getEmail()),
			exposeDevTokens ? verificationToken : null
		);
	}

	public SubscriptionVerifyResponse verify(String token) {
		String normalizedToken = normalizeToken(token);
		SubscriptionVerificationRequest verificationRequest = verificationRequestRepository
			.findByTokenHash(tokenHash(normalizedToken))
			.orElseThrow(() -> new BusinessException(SubscriptionErrorCode.SUBSCRIPTION_VERIFICATION_TOKEN_INVALID));
		Instant now = Instant.now();
		if (verificationRequest.isUsed()) {
			throw new BusinessException(SubscriptionErrorCode.SUBSCRIPTION_VERIFICATION_TOKEN_INVALID);
		}
		if (verificationRequest.isExpired(now)) {
			throw new BusinessException(SubscriptionErrorCode.SUBSCRIPTION_VERIFICATION_TOKEN_EXPIRED);
		}

		String manageToken = secureTokenGenerator.generate();
		Subscriber subscriber = verificationRequest.getSubscriber();
		subscriber.activate(tokenHash(manageToken), now);
		verificationRequest.markUsed(now);
		activatePendingSubscriptions(subscriber);

		return SubscriptionVerifyResponse.of(subscriber, manageToken);
	}

	@Transactional(readOnly = true)
	public SubscriptionManageResponse getManage(String manageToken) {
		Subscriber subscriber = findActiveSubscriberByManageToken(manageToken);
		return manageResponse(subscriber);
	}

	public SubscriptionManageResponse addCompanies(String manageToken, List<String> companySlugs) {
		Subscriber subscriber = findActiveSubscriberByManageToken(manageToken);
		List<Company> companies = findActiveCompanies(normalizeCompanySlugs(companySlugs));
		Map<String, CompanySubscription> existingSubscriptionMap = existingSubscriptionMap(subscriber);
		for (Company company : companies) {
			CompanySubscription subscription = existingSubscriptionMap.get(company.getSlug());
			if (subscription != null) {
				subscription.activate();
				continue;
			}
			companySubscriptionRepository.save(CompanySubscription.subscribe(subscriber, company));
		}
		return manageResponse(subscriber);
	}

	public SubscriptionManageResponse removeCompany(String manageToken, String companySlug) {
		Subscriber subscriber = findActiveSubscriberByManageToken(manageToken);
		String normalizedCompanySlug = normalizeCompanySlug(companySlug);
		CompanySubscription subscription = companySubscriptionRepository
			.findBySubscriber_IdAndCompany_Slug(subscriber.getId(), normalizedCompanySlug)
			.orElseThrow(() -> new BusinessException(SubscriptionErrorCode.SUBSCRIPTION_COMPANY_NOT_FOUND));
		subscription.unsubscribe();
		return manageResponse(subscriber);
	}

	public SubscriptionManageResponse unsubscribe(String manageToken) {
		Subscriber subscriber = findActiveSubscriberByManageToken(manageToken);
		companySubscriptionRepository.findBySubscriber_IdOrderByCompany_NameKoAsc(subscriber.getId())
			.forEach(CompanySubscription::unsubscribe);
		subscriber.unsubscribe();
		return manageResponse(subscriber);
	}

	private void replacePendingSubscriptions(Subscriber subscriber, List<Company> companies) {
		List<CompanySubscription> inactiveSubscriptions = companySubscriptionRepository
			.findBySubscriber_IdAndActiveFalse(subscriber.getId());
		if (!inactiveSubscriptions.isEmpty()) {
			companySubscriptionRepository.deleteAll(inactiveSubscriptions);
			companySubscriptionRepository.flush();
		}
		Map<String, CompanySubscription> existingSubscriptionMap = existingSubscriptionMap(subscriber);
		for (Company company : companies) {
			CompanySubscription subscription = existingSubscriptionMap.get(company.getSlug());
			if (subscription != null) {
				if (subscriber.getStatus() != SubscriberStatus.ACTIVE) {
					subscription.unsubscribe();
				}
				continue;
			}
			companySubscriptionRepository.save(CompanySubscription.pending(subscriber, company));
		}
	}

	private void activatePendingSubscriptions(Subscriber subscriber) {
		companySubscriptionRepository.findBySubscriber_IdAndActiveFalse(subscriber.getId())
			.forEach(CompanySubscription::activate);
	}

	private void invalidatePreviousVerificationRequests(Subscriber subscriber) {
		verificationRequestRepository.findBySubscriber_IdAndUsedAtIsNull(subscriber.getId())
			.forEach(SubscriptionVerificationRequest::invalidate);
	}

	private Subscriber findOrCreateSubscriber(String normalizedEmail) {
		return subscriberRepository.findByEmail(normalizedEmail)
			.orElseGet(() -> createSubscriber(normalizedEmail));
	}

	private Subscriber createSubscriber(String normalizedEmail) {
		try {
			return subscriberRepository.saveAndFlush(Subscriber.pending(normalizedEmail));
		} catch (DataIntegrityViolationException exception) {
			return subscriberRepository.findByEmail(normalizedEmail)
				.orElseThrow(() -> exception);
		}
	}

	private Map<String, CompanySubscription> existingSubscriptionMap(Subscriber subscriber) {
		List<CompanySubscription> existingSubscriptions = companySubscriptionRepository
			.findBySubscriber_IdOrderByCompany_NameKoAsc(subscriber.getId());
		return CollectionSupport.toMapStrict(
			existingSubscriptions,
			subscription -> subscription.getCompany().getSlug()
		);
	}

	private Subscriber findActiveSubscriberByManageToken(String manageToken) {
		return subscriberRepository.findByManageTokenHash(tokenHash(normalizeToken(manageToken)))
			.filter(subscriber -> subscriber.getStatus() == SubscriberStatus.ACTIVE)
			.orElseThrow(() -> new BusinessException(SubscriptionErrorCode.SUBSCRIPTION_MANAGE_TOKEN_NOT_FOUND));
	}

	private SubscriptionManageResponse manageResponse(Subscriber subscriber) {
		return SubscriptionManageResponse.of(
			subscriber,
			companySubscriptionRepository.findBySubscriber_IdAndActiveTrueOrderByCompany_NameKoAsc(subscriber.getId())
		);
	}

	private List<Company> findActiveCompanies(List<String> companySlugs) {
		List<Company> companies = companyRepository.findBySlugInAndStatus(companySlugs, CompanyStatus.ACTIVE);
		Map<String, Company> companyMap = CollectionSupport.toMapStrict(companies, Company::getSlug);
		if (!companyMap.keySet().containsAll(companySlugs)) {
			throw new BusinessException(SubscriptionErrorCode.SUBSCRIPTION_COMPANY_NOT_FOUND);
		}
		return companySlugs.stream()
			.map(companyMap::get)
			.toList();
	}

	private List<String> normalizeCompanySlugs(List<String> companySlugs) {
		if (CollectionSupport.isNullOrEmpty(companySlugs)) {
			throw new BusinessException(CommonErrorCode.INVALID_REQUEST, "companySlugs는 1개 이상 입력해야 합니다.");
		}
		LinkedHashSet<String> normalizedSlugs = new LinkedHashSet<>();
		for (String companySlug : companySlugs) {
			normalizedSlugs.add(normalizeCompanySlug(companySlug));
		}
		if (normalizedSlugs.size() > MAX_COMPANY_COUNT) {
			throw new BusinessException(
				CommonErrorCode.INVALID_REQUEST,
				"companySlugs는 최대 " + MAX_COMPANY_COUNT + "개까지 입력할 수 있습니다."
			);
		}
		return List.copyOf(normalizedSlugs);
	}

	private String normalizeCompanySlug(String companySlug) {
		String normalizedCompanySlug = StringNormalizer.trimToNull(companySlug);
		if (normalizedCompanySlug == null) {
			throw new BusinessException(CommonErrorCode.INVALID_REQUEST, "companySlug는 비어 있을 수 없습니다.");
		}
		return normalizedCompanySlug.toLowerCase(Locale.ROOT);
	}

	private String normalizeEmail(String email) {
		String normalizedEmail = StringNormalizer.trimToNull(email);
		if (normalizedEmail == null) {
			throw new BusinessException(CommonErrorCode.INVALID_REQUEST, "email은 필수입니다.");
		}
		return normalizedEmail.toLowerCase(Locale.ROOT);
	}

	private String normalizeToken(String token) {
		String normalizedToken = StringNormalizer.trimToNull(token);
		if (normalizedToken == null) {
			throw new BusinessException(CommonErrorCode.INVALID_REQUEST, "token은 필수입니다.");
		}
		return normalizedToken;
	}

	private String tokenHash(String token) {
		return CryptoSupport.sha256Hex(token);
	}
}
