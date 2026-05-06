"use client";

import { useMemo, useState } from "react";

import type { PublicCompanyFilterDto } from "@/features/public/types/public-api.dto";

type SubscribeFormProps = {
  companies: PublicCompanyFilterDto[];
  initialCompany?: string;
};

export function SubscribeForm({ companies, initialCompany }: SubscribeFormProps) {
  const [email, setEmail] = useState("");
  const [selected, setSelected] = useState<string[]>(initialCompany ? [initialCompany] : []);
  const [query, setQuery] = useState("");
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const filteredCompanies = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();
    if (!normalizedQuery) {
      return companies;
    }
    return companies.filter((company) => company.name.toLowerCase().includes(normalizedQuery));
  }, [companies, query]);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setMessage(null);

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setError("이메일 형식을 확인해주세요.");
      return;
    }

    if (selected.length === 0) {
      setError("알림 받을 기업을 1개 이상 선택해주세요.");
      return;
    }

    setSubmitting(true);
    try {
      const response = await fetch("/api/subscriptions/requests", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, companySlugs: selected }),
      });
      const payload = (await response.json().catch(() => undefined)) as { maskedEmail?: string; message?: string } | undefined;

      if (!response.ok) {
        setError(payload?.message ?? "구독 요청을 처리하지 못했습니다.");
        return;
      }

      setMessage(`${payload?.maskedEmail ?? "입력한 이메일"}로 확인 메일을 보냈어요.`);
    } finally {
      setSubmitting(false);
    }
  }

  function toggleCompany(slug: string) {
    setSelected((current) => (current.includes(slug) ? current.filter((item) => item !== slug) : [...current, slug]));
  }

  return (
    <form className="subscribe-form" onSubmit={handleSubmit}>
      <section className="subscribe-form__main">
        <div className="subscribe-field">
          <label htmlFor="subscribe-email">이메일</label>
          <input
            id="subscribe-email"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="name@example.com"
            autoComplete="email"
          />
        </div>

        <div className="subscribe-field">
          <label htmlFor="company-search">관심 기업</label>
          <input
            id="company-search"
            type="search"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="기업명 검색"
          />
        </div>

        <div className="subscribe-company-list">
          {filteredCompanies.map((company) => (
            <label key={company.slug} className={selected.includes(company.slug) ? "is-selected" : ""}>
              <input
                type="checkbox"
                checked={selected.includes(company.slug)}
                onChange={() => toggleCompany(company.slug)}
              />
              <span>
                <strong>{company.name}</strong>
                <em>공개 글 {company.count.toLocaleString("ko-KR")}개</em>
              </span>
            </label>
          ))}
        </div>

        {error ? <p className="subscribe-form__error">{error}</p> : null}
        {message ? <p className="subscribe-form__success">{message}</p> : null}

        <button type="submit" disabled={submitting}>
          {submitting ? "요청 중" : "확인 메일 받기"}
        </button>
      </section>

      <aside className="subscribe-form__side">
        <h2>선택한 기업</h2>
        {selected.length === 0 ? (
          <p>관심 기업을 선택해주세요.</p>
        ) : (
          <div>
            {selected.map((slug) => {
              const company = companies.find((item) => item.slug === slug);
              return <span key={slug}>{company?.name ?? slug}</span>;
            })}
          </div>
        )}
        <small>새 글이 올라오면 AI 요약과 원문 링크를 이메일로 보내드립니다.</small>
      </aside>
    </form>
  );
}
