import type {
  AdminFailureStatus,
  AdminJobStatus,
  AdminSourceStatus,
  AdminSourceType,
  ProcessingState,
  SummaryState,
  VisibilityState,
} from "@/features/admin/types/admin-api.dto";
import type { AdminStatusBadgeModel } from "@/features/admin/types/admin-view.model";

export const sourceStatusOptions: Array<{ value: AdminSourceStatus; label: string }> = [
  { value: "proposed", label: "승인 대기" },
  { value: "approved", label: "승인됨" },
  { value: "paused", label: "일시 정지" },
  { value: "rejected", label: "거절됨" },
];

export const processingStateOptions: Array<{ value: ProcessingState; label: string }> = [
  { value: "collected", label: "수집됨" },
  { value: "classified", label: "분류 완료" },
  { value: "review_required", label: "검수 필요" },
];

export const visibilityStateOptions: Array<{ value: VisibilityState; label: string }> = [
  { value: "draft", label: "초안" },
  { value: "published", label: "게시됨" },
  { value: "hidden", label: "숨김" },
  { value: "blocked", label: "차단됨" },
];

export const summaryStateOptions: Array<{ value: Exclude<SummaryState, "none">; label: string }> = [
  { value: "pending", label: "요약 대기" },
  { value: "ready", label: "요약 완료" },
  { value: "failed", label: "요약 실패" },
  { value: "hidden", label: "요약 숨김" },
];

const sourceStatusMap: Record<AdminSourceStatus, AdminStatusBadgeModel & { value: AdminSourceStatus }> = {
  proposed: { value: "proposed", label: "승인 대기", tone: "info" },
  approved: { value: "approved", label: "승인됨", tone: "success" },
  paused: { value: "paused", label: "일시 정지", tone: "warning" },
  rejected: { value: "rejected", label: "거절됨", tone: "danger" },
};

const processingStateMap: Record<ProcessingState, AdminStatusBadgeModel & { value: ProcessingState }> = {
  collected: { value: "collected", label: "수집됨", tone: "info" },
  classified: { value: "classified", label: "분류 완료", tone: "success" },
  review_required: { value: "review_required", label: "검수 필요", tone: "warning" },
};

const visibilityStateMap: Record<VisibilityState, AdminStatusBadgeModel & { value: VisibilityState }> = {
  draft: { value: "draft", label: "초안", tone: "neutral" },
  published: { value: "published", label: "게시됨", tone: "success" },
  hidden: { value: "hidden", label: "숨김", tone: "warning" },
  blocked: { value: "blocked", label: "차단됨", tone: "danger" },
};

const summaryStateMap: Record<SummaryState, AdminStatusBadgeModel & { value: SummaryState }> = {
  none: { value: "none", label: "요약 없음", tone: "neutral" },
  pending: { value: "pending", label: "요약 대기", tone: "info" },
  ready: { value: "ready", label: "요약 완료", tone: "success" },
  failed: { value: "failed", label: "요약 실패", tone: "warning" },
  hidden: { value: "hidden", label: "요약 숨김", tone: "neutral" },
};

const jobStatusMap: Record<AdminJobStatus, AdminStatusBadgeModel & { value: AdminJobStatus }> = {
  starting: { value: "starting", label: "시작 중", tone: "info" },
  started: { value: "started", label: "실행 중", tone: "info" },
  stopping: { value: "stopping", label: "중지 중", tone: "warning" },
  completed: { value: "completed", label: "완료", tone: "success" },
  failed: { value: "failed", label: "실패", tone: "danger" },
  stopped: { value: "stopped", label: "중지", tone: "warning" },
  abandoned: { value: "abandoned", label: "폐기", tone: "danger" },
  unknown: { value: "unknown", label: "알 수 없음", tone: "neutral" },
};

const failureStatusMap: Record<AdminFailureStatus, AdminStatusBadgeModel & { value: AdminFailureStatus }> = {
  failed: { value: "failed", label: "실패", tone: "danger" },
};

const sourceTypeMap: Record<AdminSourceType, string> = {
  rss: "RSS",
  atom: "Atom",
  html: "HTML",
};

export function getSourceStatusBadge(status: AdminSourceStatus) {
  return sourceStatusMap[status];
}

export function getProcessingStateBadge(state: ProcessingState) {
  return processingStateMap[state];
}

export function getVisibilityStateBadge(state: VisibilityState) {
  return visibilityStateMap[state];
}

export function getSummaryStateBadge(state: SummaryState) {
  return summaryStateMap[state];
}

export function getJobStatusBadge(status: AdminJobStatus) {
  return jobStatusMap[status] ?? jobStatusMap.unknown;
}

export function getFailureStatusBadge(status: AdminFailureStatus) {
  return failureStatusMap[status];
}

export function getSourceTypeLabel(sourceType: AdminSourceType) {
  return sourceTypeMap[sourceType];
}
