import Link from "next/link";

import { JobChip, TagChip } from "@/features/public/components/PostChips";
import type { PublicPostDetailDto, PublicPostListItemDto } from "@/features/public/types/public-api.dto";
import { formatPostDate } from "@/features/public/utils/public-format";

type PostDetailViewProps = {
  post: PublicPostDetailDto;
  relatedPosts: PublicPostListItemDto[];
};

export function PostDetailView({ post, relatedPosts }: PostDetailViewProps) {
  const summary = summaryView(post);

  return (
    <main className="post-detail-page">
      <article className="post-detail">
        <Link href="/" className="post-detail__back">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none" aria-hidden="true">
            <path d="M9 2L4 7l5 5" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
          아카이브로 돌아가기
        </Link>

        <div className="post-detail__meta">
          <Link href={`/?company=${encodeURIComponent(post.company.slug)}`}>{post.company.name}</Link>
          <span>{post.source.name}</span>
          <span>{formatPostDate(post.publishedAt)}</span>
        </div>

        <h1>{post.title}</h1>

        <div className="post-detail__chips">
          {post.jobCategories.map((job) => (
            <JobChip key={job} job={job} href={`/?job=${encodeURIComponent(job)}`} />
          ))}
          {post.topicTags.map((tag) => (
            <TagChip key={tag} tag={tag} href={`/?tag=${encodeURIComponent(tag)}`} />
          ))}
        </div>

        <div className="post-detail__actions">
          <a href={post.originUrl} target="_blank" rel="noreferrer" className="post-detail__origin">
            원문 보기
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
              <path d="M2 10L10 2M10 2H5M10 2v5" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </a>
          <Link href={`/subscribe?company=${encodeURIComponent(post.company.slug)}`} className="post-detail__subscribe">
            이 기업 구독
          </Link>
        </div>

        <section className="ai-summary-box" aria-labelledby="ai-summary-title">
          <div className="ai-summary-box__head">
            <span aria-hidden="true">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                <path d="M7 1l1.5 4H13l-3.5 2.5 1.5 4L7 9l-4 2.5 1.5-4L1 5h4.5z" fill="currentColor" />
              </svg>
            </span>
            <h2 id="ai-summary-title">AI 요약</h2>
          </div>
          <p>{summary.headline}</p>
          {summary.bullets.length > 0 ? (
            <ul>
              {summary.bullets.map((bullet) => (
                <li key={bullet}>{bullet}</li>
              ))}
            </ul>
          ) : null}
          {post.aiNotice ? <small>{post.aiNotice}</small> : null}
        </section>

        <section className="related-posts" aria-labelledby="related-posts-title">
          <h2 id="related-posts-title">같은 기업의 다른 글</h2>
          {relatedPosts.length === 0 ? (
            <p>아직 함께 볼 수 있는 공개 글이 없습니다.</p>
          ) : (
            <div className="related-posts__list">
              {relatedPosts.map((relatedPost) => (
                <Link key={relatedPost.id} href={`/posts/${relatedPost.slug}`}>
                  <span>{formatPostDate(relatedPost.publishedAt)}</span>
                  <strong>{relatedPost.title}</strong>
                </Link>
              ))}
            </div>
          )}
        </section>
      </article>
    </main>
  );
}

function summaryView(post: PublicPostDetailDto): { headline: string; bullets: string[] } {
  if (post.summaryState === "ready" && post.summary) {
    return {
      headline: post.summary.headline,
      bullets: post.summary.bullets,
    };
  }

  return {
    headline: "AI 요약을 준비하고 있어요. 원문은 바로 확인할 수 있습니다.",
    bullets: [],
  };
}
