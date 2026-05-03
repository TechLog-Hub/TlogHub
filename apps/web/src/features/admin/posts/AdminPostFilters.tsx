import Link from "next/link";

import type { AdminPostSearchQuery } from "@/features/admin/types/admin-query.model";
import {
  processingStateOptions,
  summaryStateOptions,
  visibilityStateOptions,
} from "@/features/admin/utils/admin-status";

type AdminPostFiltersProps = {
  query: AdminPostSearchQuery;
};

export function AdminPostFilters({ query }: AdminPostFiltersProps) {
  return (
    <form className="filter-bar filter-bar--posts" action="/admin/posts">
      <label className="filter-bar__field">
        <span>검색어</span>
        <input name="q" defaultValue={query.q ?? ""} placeholder="제목 또는 slug" />
      </label>
      <label className="filter-bar__field filter-bar__field--compact">
        <span>기업</span>
        <input name="company" defaultValue={query.company ?? ""} placeholder="company slug" />
      </label>
      <label className="filter-bar__field filter-bar__field--compact">
        <span>게시</span>
        <select name="visibility" defaultValue={query.visibilityState ?? ""}>
          <option value="">전체</option>
          {visibilityStateOptions.map((state) => (
            <option key={state.value} value={state.value}>
              {state.label}
            </option>
          ))}
        </select>
      </label>
      <label className="filter-bar__field filter-bar__field--compact">
        <span>처리</span>
        <select name="processing" defaultValue={query.processingState ?? ""}>
          <option value="">전체</option>
          {processingStateOptions.map((state) => (
            <option key={state.value} value={state.value}>
              {state.label}
            </option>
          ))}
        </select>
      </label>
      <label className="filter-bar__field filter-bar__field--compact">
        <span>요약</span>
        <select name="summary" defaultValue={query.summaryState ?? ""}>
          <option value="">전체</option>
          {summaryStateOptions.map((state) => (
            <option key={state.value} value={state.value}>
              {state.label}
            </option>
          ))}
        </select>
      </label>
      <input type="hidden" name="size" value={query.size} />
      <button className="button button--primary" type="submit">
        적용
      </button>
      <Link className="button button--secondary" href="/admin/posts">
        초기화
      </Link>
    </form>
  );
}
