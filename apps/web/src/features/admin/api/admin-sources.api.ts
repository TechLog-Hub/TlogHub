import "server-only";

import { adminFetch } from "@/features/admin/api/admin-fetch";
import type {
  AdminSourceListItemDto,
  PageResponse,
} from "@/features/admin/types/admin-api.dto";
import type { AdminSourceSearchQuery } from "@/features/admin/types/admin-query.model";
import { toBackendQuery } from "@/features/admin/utils/admin-query-string";

export async function getAdminSources(query: AdminSourceSearchQuery) {
  return adminFetch<PageResponse<AdminSourceListItemDto>>("/api/v1/admin/sources", {
    query: toBackendQuery(query),
  });
}
