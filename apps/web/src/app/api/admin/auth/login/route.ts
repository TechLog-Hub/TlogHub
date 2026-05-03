import { NextResponse } from "next/server";

import { loginAdmin } from "@/features/admin/api/admin-auth.api";
import {
  adminTokenCookieName,
  createAdminTokenCookieOptions,
} from "@/features/admin/auth/admin-cookie";
import { AppApiError } from "@/shared/api/api-error";

type LoginRouteRequest = {
  email?: unknown;
  password?: unknown;
};

export async function POST(request: Request) {
  const body = (await request.json().catch(() => ({}))) as LoginRouteRequest;
  const email = typeof body.email === "string" ? body.email.trim() : "";
  const password = typeof body.password === "string" ? body.password : "";

  if (!email || !password) {
    return NextResponse.json(
      { code: "INVALID_REQUEST", message: "이메일과 비밀번호를 입력해 주세요." },
      { status: 400 },
    );
  }

  try {
    const login = await loginAdmin({ email, password });
    const response = NextResponse.json({
      admin: login.admin,
      expiresAt: login.expiresAt,
    });

    response.cookies.set(
      adminTokenCookieName,
      login.accessToken,
      createAdminTokenCookieOptions(login.expiresAt),
    );
    return response;
  } catch (error) {
    if (error instanceof AppApiError) {
      return NextResponse.json(
        { code: error.code, message: error.message },
        { status: error.status },
      );
    }

    return NextResponse.json(
      { code: "INTERNAL_ERROR", message: "일시적으로 로그인할 수 없습니다." },
      { status: 500 },
    );
  }
}
