import type {
  AdminFailureStatus,
  AdminJobStatus,
  AdminSourceStatus,
  ProcessingState,
  SummaryState,
  VisibilityState,
} from "./admin-api.dto";

export type AdminStatusTone = "info" | "success" | "warning" | "danger" | "neutral";

export type AdminStatusBadgeModel = {
  value: string;
  label: string;
  tone: AdminStatusTone;
};

export type AdminSourceRow = {
  id: number;
  companyLabel: string;
  sourceLabel: string;
  homepageUrl: string;
  feedUrl: string | null;
  feedUrlLabel: string;
  sourceTypeLabel: string;
  status: AdminStatusBadgeModel & { value: AdminSourceStatus };
  lastCollectedAtLabel: string;
  createdAtLabel: string;
  reviewReasonLabel?: string;
  canRunCollection: boolean;
};

export type AdminPostRow = {
  id: number;
  title: string;
  slug: string;
  companyLabel: string;
  sourceLabel: string;
  publishedAtLabel: string;
  collectedAtLabel: string;
  processingBadge: AdminStatusBadgeModel & { value: ProcessingState };
  visibilityBadge: AdminStatusBadgeModel & { value: VisibilityState };
  summaryBadge: AdminStatusBadgeModel & { value: SummaryState };
  reviewReasonLabel?: string;
  detailHref: string;
  originHref: string;
};

export type AdminJobRunResult = {
  executionLabel: string;
  status: AdminStatusBadgeModel & { value: AdminJobStatus };
  requestedAtLabel: string;
};

export type AdminFailureRow = {
  id: number;
  sourceId: number;
  sourceLabel: string;
  status: AdminStatusBadgeModel & { value: AdminFailureStatus };
  startedAtLabel: string;
  finishedAtLabel: string;
  failureReasonLabel: string;
};
