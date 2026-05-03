package com.techloghub.api.subscription.api;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

	private final SubscriptionCommandService subscriptionCommandService;

	@PostMapping("/requests")
	public SubscriptionRequestResponse request(@Valid @RequestBody SubscriptionRequestCreateRequest request) {
		return subscriptionCommandService.request(request.email(), request.companySlugs());
	}

	@PostMapping("/verify")
	public SubscriptionVerifyResponse verify(@Valid @RequestBody SubscriptionVerifyRequest request) {
		return subscriptionCommandService.verify(request.token());
	}

	@GetMapping("/manage/{manageToken}")
	public SubscriptionManageResponse getManage(@PathVariable @NotBlank String manageToken) {
		return subscriptionCommandService.getManage(manageToken);
	}

	@PostMapping("/manage/{manageToken}/companies")
	public SubscriptionManageResponse addCompanies(
		@PathVariable @NotBlank String manageToken,
		@Valid @RequestBody SubscriptionCompanyAddRequest request
	) {
		return subscriptionCommandService.addCompanies(manageToken, request.companySlugs());
	}

	@DeleteMapping("/manage/{manageToken}/companies/{companySlug}")
	public SubscriptionManageResponse removeCompany(
		@PathVariable @NotBlank String manageToken,
		@PathVariable @NotBlank String companySlug
	) {
		return subscriptionCommandService.removeCompany(manageToken, companySlug);
	}

	@PostMapping("/manage/{manageToken}/unsubscribe")
	public SubscriptionManageResponse unsubscribe(@PathVariable @NotBlank String manageToken) {
		return subscriptionCommandService.unsubscribe(manageToken);
	}
}
