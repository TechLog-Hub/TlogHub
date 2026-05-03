import Link from "next/link";

import { AdminStatusBadge } from "@/features/admin/components/AdminStatusBadge";
import { AdminCollectionRunButton } from "@/features/admin/jobs/AdminCollectionRunButton";
import type { AdminSourceListItemDto } from "@/features/admin/types/admin-api.dto";
import { toAdminSourceRow } from "@/features/admin/utils/admin-view-mapper";

type AdminSourceTableProps = {
  sources: AdminSourceListItemDto[];
};

export function AdminSourceTable({ sources }: AdminSourceTableProps) {
  const rows = sources.map(toAdminSourceRow);

  return (
    <div className="admin-table-card">
      <table className="admin-table admin-table--sources">
        <thead>
          <tr>
            <th>기업</th>
            <th>소스명</th>
            <th>타입</th>
            <th>상태</th>
            <th>Feed</th>
            <th>마지막 수집</th>
            <th>생성일</th>
            <th>작업</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((source) => (
            <tr key={source.id}>
              <td>{source.companyLabel}</td>
              <td>
                <a href={source.homepageUrl} target="_blank" rel="noreferrer">
                  {source.sourceLabel}
                </a>
              </td>
              <td>{source.sourceTypeLabel}</td>
              <td>
                <AdminStatusBadge status={source.status} />
              </td>
              <td>
                {source.feedUrl ? (
                  <a href={source.feedUrl} target="_blank" rel="noreferrer" aria-label={`${source.feedUrlLabel} 새 탭 열기`}>
                    {source.feedUrlLabel}
                  </a>
                ) : (
                  "없음"
                )}
              </td>
              <td>{source.lastCollectedAtLabel}</td>
              <td>{source.createdAtLabel}</td>
              <td>
                <AdminCollectionRunButton
                  sourceId={source.id}
                  label="수집"
                  reason="manual-admin-ui-source"
                  variant="secondary"
                  compact
                  disabled={!source.canRunCollection}
                  disabledReason="승인된 RSS/Atom feed만 실행할 수 있습니다."
                />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <div className="admin-card-list">
        {rows.map((source) => (
          <article className="admin-list-card" key={source.id}>
            <div className="admin-list-card__head">
              <div>
                <strong>{source.sourceLabel}</strong>
                <p>{source.companyLabel}</p>
              </div>
              <AdminStatusBadge status={source.status} />
            </div>
            <dl>
              <div>
                <dt>타입</dt>
                <dd>{source.sourceTypeLabel}</dd>
              </div>
              <div>
                <dt>Feed</dt>
                <dd>{source.feedUrlLabel}</dd>
              </div>
              <div>
                <dt>마지막 수집</dt>
                <dd>{source.lastCollectedAtLabel}</dd>
              </div>
            </dl>
            <Link className="text-link" href={source.homepageUrl} target="_blank" rel="noreferrer">
              홈페이지 열기
            </Link>
            <AdminCollectionRunButton
              sourceId={source.id}
              label="이 소스 수집"
              reason="manual-admin-ui-source"
              variant="secondary"
              disabled={!source.canRunCollection}
              disabledReason="승인된 RSS/Atom feed만 실행할 수 있습니다."
            />
          </article>
        ))}
      </div>
    </div>
  );
}
