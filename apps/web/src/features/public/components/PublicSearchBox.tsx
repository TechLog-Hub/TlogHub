import type { PublicPostSearchQuery } from "@/features/public/types/public-api.dto";

type PublicSearchBoxProps = {
  query: PublicPostSearchQuery;
};

export function PublicSearchBox({ query }: PublicSearchBoxProps) {
  return (
    <section className="home-search-zone" aria-label="기술 블로그 검색">
      <form className="home-search" action="/" method="GET">
        <svg className="home-search__icon" width="20" height="20" viewBox="0 0 20 20" fill="none" aria-hidden="true">
          <circle cx="8.5" cy="8.5" r="5.5" stroke="currentColor" strokeWidth="1.8" />
          <path d="M13 13l3.5 3.5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
        </svg>
        <input type="text" name="q" defaultValue={query.q ?? ""} placeholder="회사, 기술, 글 제목 검색" aria-label="검색어" />
        <button type="submit">검색</button>
      </form>
    </section>
  );
}
