import type {
  AdminSourceStatus,
  ProcessingState,
  SummaryState,
  VisibilityState,
} from "./admin-api.dto";

export type AdminSourceSearchQuery = {
  q?: string;
  status?: AdminSourceStatus;
  page: number;
  size: number;
};

export type AdminPostSearchQuery = {
  q?: string;
  company?: string;
  processingState?: ProcessingState;
  visibilityState?: VisibilityState;
  summaryState?: Exclude<SummaryState, "none">;
  page: number;
  size: number;
};

export type SearchParamsInput = Record<string, string | string[] | undefined>;
