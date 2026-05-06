import Link from "next/link";

type EmptyStateProps = {
  title: string;
  description: string;
  resetHref?: string;
};

export function PublicEmptyState({ title, description, resetHref = "/" }: EmptyStateProps) {
  return (
    <div className="public-state">
      <strong>{title}</strong>
      <p>{description}</p>
      <Link href={resetHref} className="public-state__action">
        조건 초기화
      </Link>
    </div>
  );
}

export function PublicErrorState() {
  return (
    <div className="public-state">
      <strong>일시적으로 글을 불러오지 못했습니다.</strong>
      <p>잠시 뒤 다시 시도해주세요. 원문 출처나 내부 오류 정보는 공개 화면에 노출하지 않습니다.</p>
      <Link href="/" className="public-state__action">
        아카이브로 이동
      </Link>
    </div>
  );
}
