import Link from "next/link";
import type { ReactNode } from "react";

export type PublicNavKey = "archive" | "companies" | "tags" | "subscribe";

type PublicShellProps = {
  active: PublicNavKey;
  children: ReactNode;
};

const navItems: { key: PublicNavKey; href: string; label: string }[] = [
  { key: "archive", href: "/", label: "아카이브" },
  { key: "companies", href: "/companies", label: "기업" },
  { key: "tags", href: "/tags", label: "태그" },
  { key: "subscribe", href: "/subscribe", label: "구독" },
];

export function PublicShell({ active, children }: PublicShellProps) {
  return (
    <div className="public-page">
      <header className="site-header">
        <div className="site-header__inner">
          <Link href="/" className="site-logo" aria-label="Techlog Hub 홈">
            <span className="site-logo__mark" aria-hidden="true">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                <path d="M2 3h10M2 7h7M2 11h5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
              </svg>
            </span>
            <span>Techlog Hub</span>
          </Link>

          <nav className="site-nav" aria-label="공개 화면 내비게이션">
            {navItems.map((item) => (
              <Link
                key={item.key}
                href={item.href}
                className={item.key === active ? "site-nav__link is-active" : "site-nav__link"}
                aria-current={item.key === active ? "page" : undefined}
              >
                {item.label}
              </Link>
            ))}
          </nav>

          <Link href="/subscribe" className="site-header__cta">
            구독하기
          </Link>
        </div>
      </header>

      {children}

      <footer className="site-footer">
        <div className="site-footer__inner">
          <p>Techlog Hub는 기업 기술 블로그의 요약과 탐색 경험을 제공합니다. 원문은 각 출처에서 확인합니다.</p>
          <div className="site-footer__links">
            <Link href="/">서비스 소개</Link>
            <Link href="/subscribe">구독</Link>
            <Link href="/companies">기업</Link>
            <Link href="/tags">태그</Link>
          </div>
        </div>
      </footer>
    </div>
  );
}
