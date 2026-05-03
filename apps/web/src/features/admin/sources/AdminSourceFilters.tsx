import Link from "next/link";

import type { AdminSourceSearchQuery } from "@/features/admin/types/admin-query.model";
import { sourceStatusOptions } from "@/features/admin/utils/admin-status";

type AdminSourceFiltersProps = {
  query: AdminSourceSearchQuery;
};

export function AdminSourceFilters({ query }: AdminSourceFiltersProps) {
  return (
    <form className="filter-bar" action="/admin/sources">
      <label className="filter-bar__field">
        <span>검색어</span>
        <input name="q" defaultValue={query.q ?? ""} placeholder="기업 또는 소스명" />
      </label>
      <label className="filter-bar__field filter-bar__field--compact">
        <span>상태</span>
        <select name="status" defaultValue={query.status ?? ""}>
          <option value="">전체</option>
          {sourceStatusOptions.map((status) => (
            <option key={status.value} value={status.value}>
              {status.label}
            </option>
          ))}
        </select>
      </label>
      <input type="hidden" name="size" value={query.size} />
      <button className="button button--primary" type="submit">
        적용
      </button>
      <Link className="button button--secondary" href="/admin/sources">
        초기화
      </Link>
    </form>
  );
}
