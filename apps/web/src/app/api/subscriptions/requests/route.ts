import { NextResponse } from "next/server";

import { requestSubscription } from "@/features/public/api/public-subscriptions.api";
import { AppApiError, getSafeErrorMessage } from "@/shared/api/api-error";

export async function POST(request: Request) {
  try {
    const body = (await request.json()) as { email?: string; companySlugs?: string[] };
    const response = await requestSubscription({
      email: body.email ?? "",
      companySlugs: body.companySlugs ?? [],
    });
    return NextResponse.json(response);
  } catch (error) {
    if (error instanceof AppApiError) {
      return NextResponse.json(
        {
          code: error.code,
          message: getSafeErrorMessage(error),
          errors: error.fieldErrors,
        },
        { status: error.status },
      );
    }

    return NextResponse.json(
      {
        code: "UNKNOWN_ERROR",
        message: "구독 요청을 처리하지 못했습니다.",
      },
      { status: 500 },
    );
  }
}
