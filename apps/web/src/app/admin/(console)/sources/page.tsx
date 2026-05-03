import { getAdminSources } from "@/features/admin/api/admin-sources.api";
import { AdminEmptyState } from "@/features/admin/components/AdminEmptyState";
import { AdminPagination } from "@/features/admin/components/AdminPagination";
import { AdminSourceFilters } from "@/features/admin/sources/AdminSourceFilters";
import { AdminSourceTable } from "@/features/admin/sources/AdminSourceTable";
import type { SearchParamsInput } from "@/features/admin/types/admin-query.model";
import { parseSourceSearchQuery } from "@/features/admin/utils/admin-query-string";

export const dynamic = "force-dynamic";

type AdminSourcesPageProps = {
  searchParams: Promise<SearchParamsInput>;
};

export default async function AdminSourcesPage({ searchParams }: AdminSourcesPageProps) {
  const query = parseSourceSearchQuery(await searchParams);
  const sources = await getAdminSources(query);

  return (
    <div className="page-stack">
      <header className="page-header">
        <p>소스 관리</p>
        <h1>수집 소스</h1>
        <span>수집 대상 기술 블로그 소스 상태를 확인합니다.</span>
      </header>
      <AdminSourceFilters query={query} />
      {sources.content.length > 0 ? (
        <>
          <AdminSourceTable sources={sources.content} />
          <AdminPagination
            pathname="/admin/sources"
            page={query.page}
            totalPages={sources.totalPages}
            query={{ q: query.q, status: query.status, size: query.size }}
          />
        </>
      ) : (
        <AdminEmptyState
          title="조건에 맞는 소스가 없습니다."
          description="검색어 또는 상태 필터를 줄여 다시 확인해 주세요."
          resetHref="/admin/sources"
        />
      )}
    </div>
  );
}
