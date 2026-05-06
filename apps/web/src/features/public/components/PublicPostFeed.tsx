import Link from "next/link";

import { JobChip, TagChip } from "@/features/public/components/PostChips";
import { PublicEmptyState } from "@/features/public/components/PublicStates";
import type {
  PageResponse,
  PublicFilterMetadataDto,
  PublicPostListItemDto,
  PublicPostSearchQuery,
} from "@/features/public/types/public-api.dto";
import { companyInitials, formatPostDate, isNewPost, publicSummaryText } from "@/features/public/utils/public-format";
import { filterToggleHref, isSelected, toSearchHref } from "@/features/public/utils/public-routing";

const quickJobs = ["Frontend", "Backend", "Data", "Mobile", "Infra"];

type PublicPostFeedProps = {
  posts: PageResponse<PublicPostListItemDto>;
  filters: PublicFilterMetadataDto;
  query: PublicPostSearchQuery;
  pathname?: string;
  title?: string;
};

export function PublicPostFeed({
  posts,
  filters,
  query,
  pathname = "/",
  title = query.q ? "검색 결과" : "최신 기술 블로그",
}: PublicPostFeedProps) {
  return (
    <section className="feed-column" aria-label={title}>
      <div className="feed-toolbar">
        <div className="feed-toolbar__heading">
          <h1>{title}</h1>
          <span>총 {posts.totalElements.toLocaleString("ko-KR")}개</span>
        </div>
        <div className="feed-toolbar__sort" aria-label="정렬">
          <Link href={toSearchHref(pathname, query, { sort: "latest" })} className={query.sort === "latest" ? "is-active" : ""}>
            최신순
          </Link>
          <Link
            href={toSearchHref(pathname, query, { sort: "relevance" })}
            className={query.sort === "relevance" ? "is-active" : ""}
          >
            관련도순
          </Link>
        </div>
      </div>

      <div className="job-filter-row" aria-label="직군 빠른 필터">
        <Link href={toSearchHref(pathname, query, { job: [] })} className={(query.job ?? []).length === 0 ? "is-active" : ""}>
          전체
        </Link>
        {quickJobs.map((job) => (
          <Link
            key={job}
            href={filterToggleHref(pathname, query, "job", job)}
            className={isSelected(query.job, job) ? "is-active" : ""}
          >
            {job}
          </Link>
        ))}
      </div>

      {posts.content.length === 0 ? (
        <PublicEmptyState title="조건에 맞는 글이 없습니다." description="검색어 또는 필터를 줄여 다시 탐색해보세요." />
      ) : (
        <div className="feed-list">
          {posts.content.map((post) => (
            <FeedItem key={post.id} post={post} />
          ))}
        </div>
      )}

      <Pagination posts={posts} query={query} pathname={pathname} />

      <div className="mobile-side-panels">
        <PublicSidebar posts={posts} filters={filters} />
      </div>
    </section>
  );
}

export function PublicSidebar({
  posts,
  filters,
}: {
  posts: PageResponse<PublicPostListItemDto>;
  filters: PublicFilterMetadataDto;
}) {
  const tags = [...filters.tags].sort((a, b) => b.count - a.count).slice(0, 5);
  const companies = [...filters.companies].sort((a, b) => b.count - a.count).slice(0, 3);

  return (
    <aside className="public-sidebar" aria-label="보조 탐색 정보">
      <section className="sidebar-panel">
        <h2>오늘 많이 본 태그</h2>
        <ol className="sidebar-rank-list">
          {tags.map((tag, index) => (
            <li key={tag.slug}>
              <Link href={`/?tag=${encodeURIComponent(tag.slug)}`}>
                <span>{index + 1}</span>
                <strong>{tag.label}</strong>
                <em>글 {tag.count}</em>
              </Link>
            </li>
          ))}
        </ol>
      </section>

      <section className="sidebar-panel">
        <h2>자주 업데이트된 기업</h2>
        <div className="sidebar-company-list">
          {companies.map((company) => (
            <Link key={company.slug} href={`/?company=${encodeURIComponent(company.slug)}`}>
              <strong>{company.name}</strong>
              <span>아카이브 글 {company.count}개</span>
            </Link>
          ))}
        </div>
      </section>

      <section className="sidebar-panel sidebar-panel--subscribe">
        <h2>관심 기업 새 글 받기</h2>
        <p>새 글이 올라오면 AI 요약과 원문 링크를 이메일로 보내드립니다.</p>
        <Link href="/subscribe">구독 설정</Link>
      </section>

      <section className="sidebar-panel sidebar-panel--quiet">
        <h2>현재 탐색 범위</h2>
        <p>공개된 기술 블로그 글 {posts.totalElements.toLocaleString("ko-KR")}개를 기준으로 보여줍니다.</p>
      </section>
    </aside>
  );
}

function FeedItem({
  post,
}: {
  post: PublicPostListItemDto;
}) {
  const visibleTags = post.topicTags.slice(0, 4);
  const extraTagCount = Math.max(post.topicTags.length - visibleTags.length, 0);

  return (
    <article className="feed-item">
      <Link href={`/posts/${post.slug}`} className="feed-item__link" aria-label={`${post.title} 상세 보기`}>
        <div className="feed-item__content">
          <div className="feed-item__meta">
            <span className="feed-item__company">{post.company.name}</span>
            <span>{post.sourceName}</span>
            <span>{formatPostDate(post.publishedAt)}</span>
            {isNewPost(post.publishedAt) ? <b>NEW</b> : null}
          </div>
          <h2>{post.title}</h2>
          <p>{publicSummaryText(post.summaryState, post.summaryPreview)}</p>
          <div className="feed-item__chips">
            {post.jobCategories.slice(0, 2).map((job) => (
              <JobChip key={job} job={job} />
            ))}
            {visibleTags.map((tag) => (
              <TagChip key={tag} tag={tag} />
            ))}
            {extraTagCount > 0 ? <span className="tag-chip">+{extraTagCount}</span> : null}
          </div>
        </div>
        <div className="feed-thumb" data-seed={post.company.slug}>
          <span>{companyInitials(post)}</span>
        </div>
      </Link>
    </article>
  );
}

function Pagination({
  posts,
  query,
  pathname,
}: {
  posts: PageResponse<PublicPostListItemDto>;
  query: PublicPostSearchQuery;
  pathname: string;
}) {
  if (posts.totalPages <= 1) {
    return null;
  }

  const current = posts.page;
  const pages = Array.from({ length: posts.totalPages }, (_, index) => index).slice(
    Math.max(0, current - 2),
    Math.min(posts.totalPages, current + 3),
  );

  return (
    <nav className="public-pagination" aria-label="글 목록 페이지 이동">
      <Link
        href={toSearchHref(pathname, query, { page: Math.max(current - 1, 0) })}
        className={current === 0 ? "is-disabled" : ""}
        aria-disabled={current === 0}
      >
        이전
      </Link>
      {pages.map((page) => (
        <Link key={page} href={toSearchHref(pathname, query, { page })} className={page === current ? "is-active" : ""}>
          {page + 1}
        </Link>
      ))}
      <Link
        href={toSearchHref(pathname, query, { page: Math.min(current + 1, posts.totalPages - 1) })}
        className={current >= posts.totalPages - 1 ? "is-disabled" : ""}
        aria-disabled={current >= posts.totalPages - 1}
      >
        다음
      </Link>
    </nav>
  );
}
