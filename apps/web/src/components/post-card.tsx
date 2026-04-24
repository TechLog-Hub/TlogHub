import type { BlogEntry } from "@/data/blogs";

type PostCardProps = {
  entry: BlogEntry;
};

export function PostCard({ entry }: PostCardProps) {
  return (
    <article className="post-card">
      <div className="post-card__head">
        <span className="post-card__topic">{entry.topic}</span>
        <span className="post-card__source">{entry.source}</span>
      </div>
      <div className="post-card__body">
        <h3>{entry.title}</h3>
        <p>{entry.summary}</p>
      </div>
      <div className="post-card__footer">
        <div className="post-card__meta">
          <span>{entry.freshness}</span>
          <span>{entry.readingTime}</span>
        </div>
        <a className="post-card__link" href={entry.url} rel="noreferrer" target="_blank">
          원문 보기
        </a>
      </div>
    </article>
  );
}
