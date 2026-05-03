import { NextResponse } from "next/server";

import {
  adminTokenCookieName,
  createExpiredAdminTokenCookieOptions,
} from "@/features/admin/auth/admin-cookie";

export async function POST() {
  const response = NextResponse.json({ ok: true });
  response.cookies.set(adminTokenCookieName, "", createExpiredAdminTokenCookieOptions());
  return response;
}
