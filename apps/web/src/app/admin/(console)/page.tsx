import Link from "next/link";

export const dynamic = "force-dynamic";

export default function AdminHomePage() {
  return (
    <div className="page-stack">
      <header className="page-header">
        <p>운영 홈</p>
        <h1>현재 가능한 운영 작업</h1>
        <span>관리자 API 1차 범위에 맞춰 소스와 글 상태를 확인합니다.</span>
      </header>
      <section className="home-grid">
        <Link className="home-card" href="/admin/sources">
          <span>소스 관리</span>
          <strong>수집 대상 확인</strong>
          <p>기업 기술 블로그 소스의 승인 상태와 마지막 수집 시간을 확인합니다.</p>
        </Link>
        <Link className="home-card" href="/admin/posts">
          <span>글 관리</span>
          <strong>게시 상태 확인</strong>
          <p>수집 글의 게시, 처리, 요약 상태를 필터로 빠르게 좁혀봅니다.</p>
        </Link>
        <Link className="home-card" href="/admin/jobs">
          <span>수집 작업</span>
          <strong>RSS Batch 실행</strong>
          <p>승인된 소스를 수동 수집하고 최근 실패 사유를 확인합니다.</p>
        </Link>
      </section>
      <section className="notice-card">
        <strong>최근 운영 기준</strong>
        <p>현재 화면은 인증, 소스 목록, 글 목록/상세, RSS 수동 수집 API를 사용합니다. 승인, 게시 변경, 요약 재생성은 후속 mutation API 이후 노출합니다.</p>
      </section>
    </div>
  );
}
