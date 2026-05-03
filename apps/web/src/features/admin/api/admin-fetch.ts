import "server-only";

import { cookies } from "next/headers";

import { adminTokenCookieName } from "@/features/admin/auth/admin-cookie";
import type { ApiErrorResponse } from "@/features/admin/types/admin-api.dto";
import { AdminSessionExpiredError, AppApiError } from "@/shared/api/api-error";

type AdminFetchOptions = {
  method?: "GET" | "POST" | "PATCH" | "DELETE";
  query?: Record<string, string | number | boolean | undefined | null>;
  body?: unknown;
  cache?: RequestCache;
  token?: string;
};

export async function readAdminToken(): Promise<string | undefined> {
  return (await cookies()).get(adminTokenCookieName)?.value;
}

export async function adminFetch<T>(path: string, options: AdminFetchOptions = {}): Promise<T> {
  const token = options.token ?? (await readAdminToken());

  if (!token) {
    throw new AdminSessionExpiredError();
  }

  return requestSpringApi<T>(path, {
    ...options,
    token,
  });
}

export async function requestSpringApi<T>(path: string, options: AdminFetchOptions = {}): Promise<T> {
  const apiBaseUrl = process.env.API_BASE_URL ?? "http://localhost:8080";
  const url = new URL(path, apiBaseUrl);

  Object.entries(options.query ?? {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      url.searchParams.set(key, String(value));
    }
  });

  const response = await fetch(url, {
    method: options.method ?? "GET",
    cache: options.cache ?? "no-store",
    headers: {
      Accept: "application/json",
      ...(options.body ? { "Content-Type": "application/json" } : {}),
      ...(options.token ? { Authorization: `Bearer ${options.token}` } : {}),
    },
    body: options.body ? JSON.stringify(options.body) : undefined,
  });

  if (response.status === 401) {
    throw new AdminSessionExpiredError();
  }

  if (!response.ok) {
    throw new AppApiError(response.status, await parseErrorPayload(response));
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

async function parseErrorPayload(response: Response): Promise<ApiErrorResponse | undefined> {
  try {
    return (await response.json()) as ApiErrorResponse;
  } catch {
    return {
      code: "HTTP_ERROR",
      message: `요청이 실패했습니다. status=${response.status}`,
    };
  }
}
