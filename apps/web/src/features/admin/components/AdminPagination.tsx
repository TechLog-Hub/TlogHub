import Link from "next/link";

import { createHref } from "@/features/admin/utils/admin-query-string";

type AdminPaginationProps = {
  pathname: string;
  page: number;
  totalPages: number;
  query: Record<string, string | number | undefined>;
};

export function AdminPagination({ pathname, page, totalPages, query }: AdminPaginationProps) {
  const hasPrevious = page > 1;
  const hasNext = page < totalPages;

  return (
    <nav className="pagination" aria-label="페이지 이동">
      <Link
        className={`button button--secondary ${hasPrevious ? "" : "button--disabled"}`.trim()}
        href={hasPrevious ? createHref(pathname, { ...query, page: page - 1 }) : createHref(pathname, query)}
        aria-disabled={!hasPrevious}
      >
        이전
      </Link>
      <span className="pagination__label">
        {Math.min(page, Math.max(totalPages, 1))} / {Math.max(totalPages, 1)}
      </span>
      <Link
        className={`button button--secondary ${hasNext ? "" : "button--disabled"}`.trim()}
        href={hasNext ? createHref(pathname, { ...query, page: page + 1 }) : createHref(pathname, query)}
        aria-disabled={!hasNext}
      >
        다음
      </Link>
    </nav>
  );
}
