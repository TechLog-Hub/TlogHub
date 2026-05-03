import type {
  AdminSourceStatus,
  ProcessingState,
  SummaryState,
  VisibilityState,
} from "@/features/admin/types/admin-api.dto";
import type {
  AdminPostSearchQuery,
  AdminSourceSearchQuery,
  SearchParamsInput,
} from "@/features/admin/types/admin-query.model";

const defaultPage = 1;
const defaultSize = 50;
const maxSize = 100;

const sourceStatuses = new Set<AdminSourceStatus>(["proposed", "approved", "paused", "rejected"]);
const processingStates = new Set<ProcessingState>(["collected", "classified", "review_required"]);
const visibilityStates = new Set<VisibilityState>(["draft", "published", "hidden", "blocked"]);
const summaryStates = new Set<Exclude<SummaryState, "none">>(["pending", "ready", "failed", "hidden"]);

export function firstParam(value: string | string[] | undefined): string | undefined {
  return Array.isArray(value) ? value[0] : value;
}

function parsePositiveInt(value: string | undefined, fallback: number): number {
  if (!value) {
    return fallback;
  }

  const parsed = Number.parseInt(value, 10);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
}

function compact(value: string | undefined): string | undefined {
  const trimmed = value?.trim();
  return trimmed ? trimmed : undefined;
}

export function parseSourceSearchQuery(params: SearchParamsInput): AdminSourceSearchQuery {
  const statusValue = firstParam(params.status);
  const size = Math.min(parsePositiveInt(firstParam(params.size), defaultSize), maxSize);

  return {
    q: compact(firstParam(params.q)),
    status: statusValue && sourceStatuses.has(statusValue as AdminSourceStatus)
      ? (statusValue as AdminSourceStatus)
      : undefined,
    page: parsePositiveInt(firstParam(params.page), defaultPage),
    size,
  };
}

export function parsePostSearchQuery(params: SearchParamsInput): AdminPostSearchQuery {
  const processingValue = firstParam(params.processing);
  const visibilityValue = firstParam(params.visibility);
  const summaryValue = firstParam(params.summary);
  const size = Math.min(parsePositiveInt(firstParam(params.size), defaultSize), maxSize);

  return {
    q: compact(firstParam(params.q)),
    company: compact(firstParam(params.company)),
    processingState: processingValue && processingStates.has(processingValue as ProcessingState)
      ? (processingValue as ProcessingState)
      : undefined,
    visibilityState: visibilityValue && visibilityStates.has(visibilityValue as VisibilityState)
      ? (visibilityValue as VisibilityState)
      : undefined,
    summaryState: summaryValue && summaryStates.has(summaryValue as Exclude<SummaryState, "none">)
      ? (summaryValue as Exclude<SummaryState, "none">)
      : undefined,
    page: parsePositiveInt(firstParam(params.page), defaultPage),
    size,
  };
}

export function toApiPage(uiPage: number): number {
  return Math.max(uiPage - 1, 0);
}

export function createHref(pathname: string, query: Record<string, string | number | undefined>): string {
  const searchParams = new URLSearchParams();

  Object.entries(query).forEach(([key, value]) => {
    if (value !== undefined && value !== "") {
      searchParams.set(key, String(value));
    }
  });

  const queryString = searchParams.toString();
  return queryString ? `${pathname}?${queryString}` : pathname;
}

export function toBackendQuery(query: AdminSourceSearchQuery | AdminPostSearchQuery) {
  return {
    ...query,
    page: toApiPage(query.page),
  };
}
