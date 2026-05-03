package com.techloghub.api.subscription.api;

import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techloghub.api.common.error.BusinessException;
import com.techloghub.api.common.error.CommonErrorCode;
import com.techloghub.api.common.util.StringNormalizer;
import com.techloghub.api.subscription.application.SubscriptionCommandService;
import com.techloghub.api.subscription.dto.SubscriptionCompanyAddRequest;
import com.techloghub.api.subscription.dto.SubscriptionManageResponse;
import com.techloghub.api.subscription.dto.SubscriptionRequestCreateRequest;
import com.techloghub.api.subscription.dto.SubscriptionRequestResponse;
import com.techloghub.api.subscription.dto.SubscriptionVerifyRequest;
import com.techloghub.api.subscription.dto.SubscriptionVerifyResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

/**
 * 이메일 기반 기업 구독 API다.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

	private static final String BEARER_PREFIX = "Bearer ";

	private final SubscriptionCommandService subscriptionCommandService;

	@PostMapping("/requests")
	public SubscriptionRequestResponse request(@Valid @RequestBody SubscriptionRequestCreateRequest request) {
		return subscriptionCommandService.request(request.email(), request.companySlugs());
	}

	@PostMapping("/verify")
	public SubscriptionVerifyResponse verify(@Valid @RequestBody SubscriptionVerifyRequest request) {
		return subscriptionCommandService.verify(request.token());
	}

	@GetMapping("/manage")
	public SubscriptionManageResponse getManage(
		@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
	) {
		return subscriptionCommandService.getManage(bearerToken(authorization));
	}

	@PostMapping("/manage/companies")
	public SubscriptionManageResponse addCompanies(
		@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
		@Valid @RequestBody SubscriptionCompanyAddRequest request
	) {
		return subscriptionCommandService.addCompanies(bearerToken(authorization), request.companySlugs());
	}

	@DeleteMapping("/manage/companies/{companySlug}")
	public SubscriptionManageResponse removeCompany(
		@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
		@PathVariable @NotBlank String companySlug
	) {
		return subscriptionCommandService.removeCompany(bearerToken(authorization), companySlug);
	}

	@PostMapping("/manage/unsubscribe")
	public SubscriptionManageResponse unsubscribe(
		@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
	) {
		return subscriptionCommandService.unsubscribe(bearerToken(authorization));
	}

	private String bearerToken(String authorization) {
		String normalizedAuthorization = StringNormalizer.trimToNull(authorization);
		if (normalizedAuthorization == null || !normalizedAuthorization.startsWith(BEARER_PREFIX)) {
			throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
		}
		String token = StringNormalizer.trimToNull(normalizedAuthorization.substring(BEARER_PREFIX.length()));
		if (token == null) {
			throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
		}
		return token;
	}
}
