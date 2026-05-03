import "server-only";

import { redirect } from "next/navigation";

import { adminFetch, readAdminToken, requestSpringApi } from "@/features/admin/api/admin-fetch";
import type {
  AdminLoginRequestDto,
  AdminLoginResponseDto,
  AdminMeDto,
} from "@/features/admin/types/admin-api.dto";
import { AdminSessionExpiredError } from "@/shared/api/api-error";

export async function loginAdmin(credentials: AdminLoginRequestDto): Promise<AdminLoginResponseDto> {
  return requestSpringApi<AdminLoginResponseDto>("/api/v1/admin/auth/login", {
    method: "POST",
    body: credentials,
  });
}

export async function logoutAdmin(token: string): Promise<void> {
  await requestSpringApi<void>("/api/v1/admin/auth/logout", {
    method: "POST",
    token,
  });
}

export async function getAdminMe(): Promise<AdminMeDto> {
  return adminFetch<AdminMeDto>("/api/v1/admin/auth/me");
}

export async function getOptionalAdminMe(): Promise<AdminMeDto | null> {
  const token = await readAdminToken();

  if (!token) {
    return null;
  }

  try {
    return await adminFetch<AdminMeDto>("/api/v1/admin/auth/me", { token });
  } catch {
    return null;
  }
}

export async function requireAdminSession(): Promise<AdminMeDto> {
  try {
    return await getAdminMe();
  } catch (error) {
    if (error instanceof AdminSessionExpiredError) {
      redirect("/admin/login");
    }

    throw error;
  }
}
