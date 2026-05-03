import { cookies } from "next/headers";
import { NextResponse } from "next/server";

import { logoutAdmin } from "@/features/admin/api/admin-auth.api";
import {
  adminTokenCookieName,
  createExpiredAdminTokenCookieOptions,
} from "@/features/admin/auth/admin-cookie";

export async function POST() {
  const token = (await cookies()).get(adminTokenCookieName)?.value;

  if (token) {
    await logoutAdmin(token).catch(() => undefined);
  }

  const response = NextResponse.json({ ok: true });
  response.cookies.set(adminTokenCookieName, "", createExpiredAdminTokenCookieOptions());
  return response;
}
