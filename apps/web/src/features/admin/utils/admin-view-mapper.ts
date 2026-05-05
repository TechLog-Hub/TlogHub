import type {
  AdminFailureDto,
  AdminJobRunDto,
  AdminPostListItemDto,
  AdminSourceListItemDto,
} from "@/features/admin/types/admin-api.dto";
import type {
  AdminFailureRow,
  AdminJobRunResult,
  AdminPostRow,
  AdminSourceRow,
} from "@/features/admin/types/admin-view.model";
import { formatAdminDate, formatAdminDateTime } from "@/features/admin/utils/admin-date";
import {
  getFailureStatusBadge,
  getJobStatusBadge,
  getProcessingStateBadge,
  getSourceStatusBadge,
  getSourceTypeLabel,
  getSummaryStateBadge,
  getVisibilityStateBadge,
} from "@/features/admin/utils/admin-status";

export function toAdminSourceRow(source: AdminSourceListItemDto): AdminSourceRow {
  return {
    id: source.id,
    companyLabel: source.companyName,
    sourceLabel: source.name,
    homepageUrl: source.homepageUrl,
    feedUrl: source.feedUrl,
    feedUrlLabel: source.feedUrl ? toDomainLabel(source.feedUrl) : "없음",
    sourceTypeLabel: getSourceTypeLabel(source.sourceType),
    status: getSourceStatusBadge(source.status),
    lastCollectedAtLabel: formatAdminDateTime(source.lastCollectedAt, "수집 전"),
    createdAtLabel: formatAdminDateTime(source.createdAt),
    reviewReasonLabel: source.reviewReason ?? undefined,
    canRunCollection:
      source.status === "approved" &&
      Boolean(source.feedUrl) &&
      ["rss", "atom"].includes(source.sourceType),
  };
}

export function toAdminPostRow(post: AdminPostListItemDto): AdminPostRow {
  return {
    id: post.id,
    title: post.title,
    slug: post.slug,
    companyLabel: post.companyName,
    sourceLabel: post.sourceName,
    publishedAtLabel: formatAdminDate(post.publishedAt),
    collectedAtLabel: formatAdminDateTime(post.collectedAt),
    processingBadge: getProcessingStateBadge(post.processingState),
    visibilityBadge: getVisibilityStateBadge(post.visibilityState),
    summaryBadge: getSummaryStateBadge(post.summaryState),
    reviewReasonLabel: post.reviewReason ?? undefined,
    detailHref: `/admin/posts/${post.id}`,
    originHref: post.originUrl,
  };
}

export function toDomainLabel(url: string): string {
  try {
    return new URL(url).hostname.replace(/^www\./, "");
  } catch {
    return url;
  }
}

export function toAdminJobRunResult(result: AdminJobRunDto): AdminJobRunResult {
  return {
    executionLabel: `#${result.executionId}`,
    status: getJobStatusBadge(result.status),
    requestedAtLabel: formatAdminDateTime(result.requestedAt),
  };
}

export function toAdminFailureRow(failure: AdminFailureDto): AdminFailureRow {
  return {
    id: failure.collectionRunId,
    sourceId: failure.sourceBlogId,
    sourceLabel: failure.sourceName,
    status: getFailureStatusBadge(failure.status),
    startedAtLabel: formatAdminDateTime(failure.startedAt),
    finishedAtLabel: formatAdminDateTime(failure.finishedAt, "종료 전"),
    failureReasonLabel: failure.failureReason ?? "실패 사유가 기록되지 않았습니다.",
  };
}
