import { publicFetch } from "@/features/public/api/public-fetch";
import type {
  PageResponse,
  PublicFilterMetadataDto,
  PublicPostDetailDto,
  PublicPostListItemDto,
  PublicPostSearchQuery,
} from "@/features/public/types/public-api.dto";

export function getPublicPosts(query: PublicPostSearchQuery = {}): Promise<PageResponse<PublicPostListItemDto>> {
  return publicFetch<PageResponse<PublicPostListItemDto>>("/api/v1/public/posts", {
    query: {
      q: query.q,
      company: query.company,
      job: query.job,
      tag: query.tag,
      sort: query.sort,
      page: query.page,
      size: query.size,
    },
  });
}

export function getPublicPost(slug: string): Promise<PublicPostDetailDto> {
  return publicFetch<PublicPostDetailDto>(`/api/v1/public/posts/${encodeURIComponent(slug)}`);
}

export function getPublicFilters(): Promise<PublicFilterMetadataDto> {
  return publicFetch<PublicFilterMetadataDto>("/api/v1/public/filters");
}
