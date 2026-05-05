import { NextResponse } from "next/server";

import { getAdminJobExecution } from "@/features/admin/api/admin-jobs.api";
import { AdminSessionExpiredError, AppApiError } from "@/shared/api/api-error";

type JobExecutionRouteContext = {
  params: Promise<{
    executionId: string;
  }>;
};

export async function GET(_request: Request, context: JobExecutionRouteContext) {
  const { executionId } = await context.params;
  const normalizedExecutionId = Number(executionId);

  if (!Number.isInteger(normalizedExecutionId) || normalizedExecutionId <= 0) {
    return NextResponse.json(
      { code: "INVALID_REQUEST", message: "executionId는 양수 정수여야 합니다." },
      { status: 400 },
    );
  }

  try {
    const result = await getAdminJobExecution(normalizedExecutionId);
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
      { code: "INTERNAL_ERROR", message: "작업 실행 상태를 조회하지 못했습니다." },
      { status: 500 },
    );
  }
}
