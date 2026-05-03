import Link from "next/link";
import type { ReactNode } from "react";

import { AdminStatusBadge } from "@/features/admin/components/AdminStatusBadge";
import type { AdminPostDetailDto } from "@/features/admin/types/admin-api.dto";
import { formatAdminDateTime } from "@/features/admin/utils/admin-date";
import {
  getProcessingStateBadge,
  getVisibilityStateBadge,
} from "@/features/admin/utils/admin-status";

type AdminPostDetailPanelProps = {
  post: AdminPostDetailDto;
};

export function AdminPostDetailPanel({ post }: AdminPostDetailPanelProps) {
  return (
    <div className="detail-layout">
      <section className="detail-main">
        <div className="detail-main__head">
          <p>
            {post.companyName} · {post.sourceName}
          </p>
          <h1>{post.title}</h1>
        </div>
        <InfoSection title="URL">
          <Definition label="원문 URL">
            <a href={post.originUrl} target="_blank" rel="noreferrer">
              {post.originUrl}
            </a>
          </Definition>
          <Definition label="Canonical URL">
            <a href={post.canonicalUrl} target="_blank" rel="noreferrer">
              {post.canonicalUrl}
            </a>
          </Definition>
        </InfoSection>
        <InfoSection title="시간">
          <Definition label="발행 시각">{formatAdminDateTime(post.publishedAt)}</Definition>
          <Definition label="수집 시각">{formatAdminDateTime(post.collectedAt)}</Definition>
        </InfoSection>
        <InfoSection title="분류">
          <Definition label="직군">
            <ChipList emptyLabel="미분류" values={post.jobCategories} />
          </Definition>
          <Definition label="태그">
            <ChipList emptyLabel="태그 없음" values={post.topicTags} />
          </Definition>
        </InfoSection>
        {post.reviewReason ? (
          <InfoSection title="검토 사유">
            <p className="detail-note">{post.reviewReason}</p>
          </InfoSection>
        ) : null}
      </section>
      <aside className="detail-side">
        <div className="detail-side__card">
          <h2>운영 상태</h2>
          <div className="detail-side__badges">
            <AdminStatusBadge status={getVisibilityStateBadge(post.visibilityState)} />
            <AdminStatusBadge status={getProcessingStateBadge(post.processingState)} />
          </div>
          <a className="button button--primary" href={post.originUrl} target="_blank" rel="noreferrer">
            원문 열기
          </a>
          <Link className="button button--secondary" href="/admin/posts">
            목록으로
          </Link>
        </div>
      </aside>
    </div>
  );
}

function InfoSection({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section className="detail-section">
      <h2>{title}</h2>
      <dl>{children}</dl>
    </section>
  );
}

function Definition({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div>
      <dt>{label}</dt>
      <dd>{children}</dd>
    </div>
  );
}

function ChipList({ values, emptyLabel }: { values: string[]; emptyLabel: string }) {
  if (values.length === 0) {
    return <span className="muted-text">{emptyLabel}</span>;
  }

  return (
    <div className="chip-list">
      {values.map((value) => (
        <span className="chip" key={value}>
          {value}
        </span>
      ))}
    </div>
  );
}
