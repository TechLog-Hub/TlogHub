import "server-only";

import { AppApiError } from "@/shared/api/api-error";
import type { ApiErrorResponse } from "@/features/admin/types/admin-api.dto";

export type PublicFetchOptions = {
  method?: "GET" | "POST" | "DELETE";
  query?: Record<string, string | number | boolean | string[] | undefined | null>;
  body?: unknown;
  cache?: RequestCache;
  headers?: HeadersInit;
};

export async function publicFetch<T>(path: string, options: PublicFetchOptions = {}): Promise<T> {
  const apiBaseUrl = process.env.API_BASE_URL ?? "http://localhost:8080";
  const url = new URL(path, apiBaseUrl);

  Object.entries(options.query ?? {}).forEach(([key, value]) => {
    if (Array.isArray(value)) {
      value.filter(Boolean).forEach((item) => url.searchParams.append(key, item));
      return;
    }

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
      ...options.headers,
    },
    body: options.body ? JSON.stringify(options.body) : undefined,
  });

  if (!response.ok) {
    throw new AppApiError(response.status, await parseErrorPayload(response));
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  return text ? (JSON.parse(text) as T) : (undefined as T);
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
