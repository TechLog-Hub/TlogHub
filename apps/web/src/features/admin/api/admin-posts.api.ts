import "server-only";

import { adminFetch } from "@/features/admin/api/admin-fetch";
import type {
  AdminPostDetailDto,
  AdminPostListItemDto,
  PageResponse,
} from "@/features/admin/types/admin-api.dto";
import type { AdminPostSearchQuery } from "@/features/admin/types/admin-query.model";
import { toBackendQuery } from "@/features/admin/utils/admin-query-string";

export async function getAdminPosts(query: AdminPostSearchQuery) {
  return adminFetch<PageResponse<AdminPostListItemDto>>("/api/v1/admin/posts", {
    query: toBackendQuery(query),
  });
}

export async function getAdminPostDetail(postId: string) {
  return adminFetch<AdminPostDetailDto>(`/api/v1/admin/posts/${postId}`);
}
