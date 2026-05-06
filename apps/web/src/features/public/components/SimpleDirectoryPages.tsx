import Link from "next/link";

import type { PublicCompanyFilterDto, PublicTagFilterDto } from "@/features/public/types/public-api.dto";

export function CompanyDirectory({ companies }: { companies: PublicCompanyFilterDto[] }) {
  return (
    <main className="directory-page">
      <div className="directory-page__head">
        <h1>기업 기술 블로그</h1>
        <p>관심 기업을 선택해 해당 기업의 공개 글만 빠르게 볼 수 있습니다.</p>
      </div>
      <div className="company-card-grid">
        {companies.map((company) => (
          <Link key={company.slug} href={`/?company=${encodeURIComponent(company.slug)}`} className="company-card">
            <span className="company-card__logo">{company.name.slice(0, 2)}</span>
            <strong>{company.name}</strong>
            <p>아카이브된 공개 글 {company.count.toLocaleString("ko-KR")}개</p>
            <em>글 보기</em>
          </Link>
        ))}
      </div>
    </main>
  );
}

export function TagDirectory({ tags }: { tags: PublicTagFilterDto[] }) {
  return (
    <main className="directory-page">
      <div className="directory-page__head">
        <h1>기술 태그</h1>
        <p>기술 주제를 기준으로 기업 기술 블로그 글을 좁혀볼 수 있습니다.</p>
      </div>
      <div className="tag-card-grid">
        {tags.map((tag) => (
          <Link key={tag.slug} href={`/?tag=${encodeURIComponent(tag.slug)}`} className="tag-card">
            <span>글 {tag.count.toLocaleString("ko-KR")}개</span>
            <strong>{tag.label}</strong>
            <p>{tag.label} 주제와 연결된 최신 기술 블로그 글을 확인합니다.</p>
          </Link>
        ))}
      </div>
    </main>
  );
}
