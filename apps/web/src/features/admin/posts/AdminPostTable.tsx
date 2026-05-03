import Link from "next/link";

import { AdminStatusBadge } from "@/features/admin/components/AdminStatusBadge";
import type { AdminPostListItemDto } from "@/features/admin/types/admin-api.dto";
import { toAdminPostRow } from "@/features/admin/utils/admin-view-mapper";

type AdminPostTableProps = {
  posts: AdminPostListItemDto[];
};

export function AdminPostTable({ posts }: AdminPostTableProps) {
  const rows = posts.map(toAdminPostRow);

  return (
    <div className="admin-table-card">
      <table className="admin-table admin-table--posts">
        <thead>
          <tr>
            <th>제목</th>
            <th>기업</th>
            <th>출처</th>
            <th>발행일</th>
            <th>게시</th>
            <th>처리</th>
            <th>요약</th>
            <th>원문</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((post) => (
            <tr key={post.id}>
              <td>
                <Link className="admin-table__title" href={post.detailHref}>
                  {post.title}
                  <span>{post.slug}</span>
                </Link>
              </td>
              <td>{post.companyLabel}</td>
              <td>{post.sourceLabel}</td>
              <td>{post.publishedAtLabel}</td>
              <td>
                <AdminStatusBadge status={post.visibilityBadge} />
              </td>
              <td>
                <AdminStatusBadge status={post.processingBadge} />
              </td>
              <td>
                <AdminStatusBadge status={post.summaryBadge} />
              </td>
              <td>
                <a href={post.originHref} target="_blank" rel="noreferrer" aria-label={`${post.title} 원문 새 탭 열기`}>
                  원문
                </a>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <div className="admin-card-list">
        {rows.map((post) => (
          <article className="admin-list-card" key={post.id}>
            <div className="admin-list-card__head">
              <div>
                <strong>{post.title}</strong>
                <p>
                  {post.companyLabel} · {post.sourceLabel} · {post.publishedAtLabel}
                </p>
              </div>
            </div>
            <div className="admin-list-card__badges">
              <AdminStatusBadge status={post.visibilityBadge} />
              <AdminStatusBadge status={post.processingBadge} />
              <AdminStatusBadge status={post.summaryBadge} />
            </div>
            <div className="admin-list-card__actions">
              <Link className="text-link" href={post.detailHref}>
                상세 보기
              </Link>
              <Link className="text-link" href={post.originHref} target="_blank" rel="noreferrer">
                원문 열기
              </Link>
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}
