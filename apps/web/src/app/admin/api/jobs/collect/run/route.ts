import { NextResponse } from "next/server";

import { runAdminCollectionJob } from "@/features/admin/api/admin-jobs.api";
import { AppApiError, AdminSessionExpiredError } from "@/shared/api/api-error";

type CollectionRunRouteRequest = {
  sourceId?: unknown;
  reason?: unknown;
};

const defaultReason = "manual-admin-ui";

export async function POST(request: Request) {
  const body = ((await request.json().catch(() => ({}))) ?? {}) as CollectionRunRouteRequest;
  const sourceIdResult = normalizeSourceId(body.sourceId);
  if (!sourceIdResult.ok) {
    return NextResponse.json(
      { code: "INVALID_REQUEST", message: "sourceId는 양수 정수여야 합니다." },
      { status: 400 },
    );
  }

  const reason = normalizeReason(body.reason);

  try {
    const result = await runAdminCollectionJob({
      sourceId: sourceIdResult.value,
      reason,
    });
    return NextResponse.json(result);
  } catch (error) {
    if (error instanceof AdminSessionExpiredError) {
      return NextResponse.json(
        { code: "ADMIN_SESSION_INVALID", message: "관리자 세션이 만료되었습니다." },
        { status: 401 },
      );
    }

    if (error instanceof AppApiError) {
      return NextResponse.json(
        { code: error.code, message: error.message },
        { status: error.status },
      );
    }

    return NextResponse.json(
      { code: "INTERNAL_ERROR", message: "수집 작업을 실행하지 못했습니다." },
      { status: 500 },
    );
  }
}

function normalizeSourceId(value: unknown): { ok: true; value?: number } | { ok: false } {
  if (value === undefined || value === null) {
    return { ok: true };
  }

  if (typeof value !== "number" || !Number.isInteger(value) || value <= 0) {
    return { ok: false };
  }

  return { ok: true, value };
}

function normalizeReason(value: unknown): string {
  if (typeof value !== "string") {
    return defaultReason;
  }

  const trimmed = value.trim();
  return trimmed ? trimmed.slice(0, 80) : defaultReason;
}
