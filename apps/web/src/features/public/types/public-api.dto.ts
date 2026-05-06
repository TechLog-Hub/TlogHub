export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type PublicCompanySummaryDto = {
  slug: string;
  name: string;
};

export type PublicSourceSummaryDto = {
  name: string;
  url: string;
};

export type PublicPostSummaryDto = {
  headline: string;
  bullets: string[];
};

export type PublicPostListItemDto = {
  id: number;
  slug: string;
  title: string;
  company: PublicCompanySummaryDto;
  sourceName: string;
  publishedAt: string;
  jobCategories: string[];
  topicTags: string[];
  summaryState: PublicSummaryState;
  summaryPreview: string;
  originUrl: string;
};

export type PublicPostDetailDto = {
  id: number;
  slug: string;
  title: string;
  company: PublicCompanySummaryDto;
  source: PublicSourceSummaryDto;
  publishedAt: string;
  jobCategories: string[];
  topicTags: string[];
  summaryState: PublicSummaryState;
  summary?: PublicPostSummaryDto;
  originUrl: string;
  aiNotice?: string;
};

export type PublicSummaryState = "ready" | "pending" | "failed" | "hidden" | "none";

export type PublicCompanyFilterDto = {
  slug: string;
  name: string;
  count: number;
};

export type PublicJobFilterDto = {
  code: string;
  label: string;
  count: number;
};

export type PublicTagFilterDto = {
  slug: string;
  label: string;
  count: number;
};

export type PublicFilterMetadataDto = {
  companies: PublicCompanyFilterDto[];
  jobs: PublicJobFilterDto[];
  tags: PublicTagFilterDto[];
};

export type PublicPostSort = "latest" | "relevance";

export type PublicPostSearchQuery = {
  q?: string;
  company?: string[];
  job?: string[];
  tag?: string[];
  sort?: PublicPostSort;
  page?: number;
  size?: number;
};

export type SubscriptionRequestDto = {
  email: string;
  companySlugs: string[];
};

export type SubscriptionRequestResponseDto = {
  requestAccepted: boolean;
  maskedEmail: string;
  verificationToken?: string;
};
