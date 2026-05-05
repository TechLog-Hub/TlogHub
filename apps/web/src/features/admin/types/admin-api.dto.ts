export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type ApiFieldErrorDto = {
  field: string;
  reason: string;
  rejectedValue?: string;
};

export type ApiErrorResponse = {
  code: string;
  message: string;
  errors?: ApiFieldErrorDto[];
  traceId?: string;
};

export type AdminMeDto = {
  id: number;
  email: string;
  role: "admin";
};

export type AdminLoginRequestDto = {
  email: string;
  password: string;
};

export type AdminLoginResponseDto = {
  accessToken: string;
  tokenType: "Bearer";
  expiresAt: string;
  admin: AdminMeDto;
};

export type AdminSourceStatus = "proposed" | "approved" | "paused" | "rejected";

export type AdminSourceType = "rss" | "atom" | "html";

export type AdminSourceListItemDto = {
  id: number;
  companySlug: string;
  companyName: string;
  name: string;
  homepageUrl: string;
  feedUrl: string | null;
  sourceType: AdminSourceType;
  status: AdminSourceStatus;
  reviewReason: string | null;
  lastCollectedAt: string | null;
  createdAt: string;
};

export type ProcessingState = "collected" | "classified" | "review_required";

export type VisibilityState = "draft" | "published" | "hidden" | "blocked";

export type SummaryState = "none" | "pending" | "ready" | "failed" | "hidden";

export type AdminPostListItemDto = {
  id: number;
  slug: string;
  title: string;
  originUrl: string;
  companySlug: string;
  companyName: string;
  sourceName: string;
  publishedAt: string;
  collectedAt: string;
  processingState: ProcessingState;
  visibilityState: VisibilityState;
  summaryState: SummaryState;
  reviewReason: string | null;
};

export type AdminPostDetailDto = {
  id: number;
  slug: string;
  title: string;
  originUrl: string;
  canonicalUrl: string;
  companySlug: string;
  companyName: string;
  sourceName: string;
  publishedAt: string;
  collectedAt: string;
  processingState: ProcessingState;
  visibilityState: VisibilityState;
  reviewReason: string | null;
  jobCategories: string[];
  topicTags: string[];
};

export type AdminJobStatus =
  | "starting"
  | "started"
  | "stopping"
  | "completed"
  | "failed"
  | "stopped"
  | "abandoned"
  | "unknown";

export type AdminJobRunDto = {
  jobName: string;
  executionId: number;
  status: AdminJobStatus;
  requestedAt: string;
};

export type AdminFailureStatus = "failed";

export type AdminFailureDto = {
  collectionRunId: number;
  sourceBlogId: number;
  sourceName: string;
  status: AdminFailureStatus;
  startedAt: string;
  finishedAt: string | null;
  failureReason: string | null;
};
