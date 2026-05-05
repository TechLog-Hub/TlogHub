import { getAdminCollectionFailures } from "@/features/admin/api/admin-jobs.api";
import { AdminEmptyState } from "@/features/admin/components/AdminEmptyState";
import { AdminCollectionRunButton } from "@/features/admin/jobs/AdminCollectionRunButton";
import { AdminFailureList } from "@/features/admin/jobs/AdminFailureList";

export const dynamic = "force-dynamic";

export default async function AdminJobsPage() {
  const failures = await getAdminCollectionFailures(20);

  return (
    <div className="page-stack">
      <header className="page-header">
        <p>수집 작업</p>
        <h1>RSS Batch 운영</h1>
        <span>승인된 RSS/Atom 소스 수집을 수동 실행하고 최근 실패 이력을 확인합니다.</span>
      </header>

      <section className="operation-panel">
        <div>
          <p>전체 승인 소스</p>
          <h2>수집 job 실행</h2>
          <span>실행 요청은 비동기로 접수됩니다. 완료 여부는 실패 목록과 소스의 마지막 수집 시각으로 확인합니다.</span>
        </div>
        <AdminCollectionRunButton label="전체 수집 실행" reason="manual-admin-ui-all" />
      </section>

      <section className="page-stack">
        <header className="section-header">
          <div>
            <p>최근 실패</p>
            <h2>수집 실패 이력</h2>
          </div>
          <span>최근 20건 기준</span>
        </header>
        {failures.length > 0 ? (
          <AdminFailureList failures={failures} />
        ) : (
          <AdminEmptyState
            title="최근 수집 실패가 없습니다."
            description="승인된 소스 수집이 실패하면 이 영역에 source와 실패 사유가 표시됩니다."
          />
        )}
      </section>
    </div>
  );
}
