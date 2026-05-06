import { publicFetch } from "@/features/public/api/public-fetch";
import type {
  SubscriptionRequestDto,
  SubscriptionRequestResponseDto,
} from "@/features/public/types/public-api.dto";

export function requestSubscription(body: SubscriptionRequestDto): Promise<SubscriptionRequestResponseDto> {
  return publicFetch<SubscriptionRequestResponseDto>("/api/v1/subscriptions/requests", {
    method: "POST",
    body,
  });
}
