import { AdminStatusBadge } from "@/features/admin/components/AdminStatusBadge";
import type { AdminFailureDto } from "@/features/admin/types/admin-api.dto";
import { toAdminFailureRow } from "@/features/admin/utils/admin-view-mapper";

type AdminFailureListProps = {
  failures: AdminFailureDto[];
};

export function AdminFailureList({ failures }: AdminFailureListProps) {
  const rows = failures.map(toAdminFailureRow);

  return (
    <div className="admin-table-card">
      <table className="admin-table admin-table--failures">
        <thead>
          <tr>
            <th>Source</th>
            <th>상태</th>
            <th>시작</th>
            <th>종료</th>
            <th>실패 사유</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((failure) => (
            <tr key={failure.id}>
              <td>
                <strong>{failure.sourceLabel}</strong>
                <span className="admin-table__subtext">source #{failure.sourceId}</span>
              </td>
              <td>
                <AdminStatusBadge status={failure.status} />
              </td>
              <td>{failure.startedAtLabel}</td>
              <td>{failure.finishedAtLabel}</td>
              <td>
                <span className="admin-table__reason">{failure.failureReasonLabel}</span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <div className="admin-card-list">
        {rows.map((failure) => (
          <article className="admin-list-card" key={failure.id}>
            <div className="admin-list-card__head">
              <div>
                <strong>{failure.sourceLabel}</strong>
                <p>source #{failure.sourceId}</p>
              </div>
              <AdminStatusBadge status={failure.status} />
            </div>
            <dl>
              <div>
                <dt>시작</dt>
                <dd>{failure.startedAtLabel}</dd>
              </div>
              <div>
                <dt>종료</dt>
                <dd>{failure.finishedAtLabel}</dd>
              </div>
              <div>
                <dt>사유</dt>
                <dd>{failure.failureReasonLabel}</dd>
              </div>
            </dl>
          </article>
        ))}
      </div>
    </div>
  );
}
