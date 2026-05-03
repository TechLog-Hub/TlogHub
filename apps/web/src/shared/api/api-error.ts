import type { ApiErrorResponse } from "@/features/admin/types/admin-api.dto";

export class AppApiError extends Error {
  readonly status: number;
  readonly code: string;
  readonly traceId?: string;
  readonly fieldErrors: ApiErrorResponse["errors"];

  constructor(status: number, payload?: Partial<ApiErrorResponse>) {
    super(payload?.message ?? "요청을 처리하지 못했습니다.");
    this.name = "AppApiError";
    this.status = status;
    this.code = payload?.code ?? "UNKNOWN_ERROR";
    this.traceId = payload?.traceId;
    this.fieldErrors = payload?.errors;
  }
}

export class AdminSessionExpiredError extends Error {
  constructor() {
    super("관리자 세션이 만료되었습니다.");
    this.name = "AdminSessionExpiredError";
  }
}

export function getSafeErrorMessage(error: unknown): string {
  if (error instanceof AppApiError) {
    if (error.status >= 500) {
      return "일시적으로 요청을 처리할 수 없습니다.";
    }

    return error.message;
  }

  if (error instanceof Error) {
    return error.message;
  }

  return "알 수 없는 오류가 발생했습니다.";
}
