import { getAdminPosts } from "@/features/admin/api/admin-posts.api";
import { AdminEmptyState } from "@/features/admin/components/AdminEmptyState";
import { AdminPagination } from "@/features/admin/components/AdminPagination";
import { AdminPostFilters } from "@/features/admin/posts/AdminPostFilters";
import { AdminPostTable } from "@/features/admin/posts/AdminPostTable";
import type { SearchParamsInput } from "@/features/admin/types/admin-query.model";
import { parsePostSearchQuery } from "@/features/admin/utils/admin-query-string";

export const dynamic = "force-dynamic";

type AdminPostsPageProps = {
  searchParams: Promise<SearchParamsInput>;
};

export default async function AdminPostsPage({ searchParams }: AdminPostsPageProps) {
  const query = parsePostSearchQuery(await searchParams);
  const posts = await getAdminPosts(query);

  return (
    <div className="page-stack">
      <header className="page-header">
        <p>글 관리</p>
        <h1>수집 글</h1>
        <span>게시, 처리, 요약 상태를 조합해 운영 대상을 확인합니다.</span>
      </header>
      <AdminPostFilters query={query} />
      {posts.content.length > 0 ? (
        <>
          <AdminPostTable posts={posts.content} />
          <AdminPagination
            pathname="/admin/posts"
            page={query.page}
            totalPages={posts.totalPages}
            query={{
              q: query.q,
              company: query.company,
              processing: query.processingState,
              visibility: query.visibilityState,
              summary: query.summaryState,
              size: query.size,
            }}
          />
        </>
      ) : (
        <AdminEmptyState
          title="조건에 맞는 글이 없습니다."
          description="검색어, 기업, 상태 필터를 줄여 다시 확인해 주세요."
          resetHref="/admin/posts"
        />
      )}
    </div>
  );
}
