"use client";

import { useDeferredValue, useState } from "react";
import { PostCard } from "@/components/post-card";
import type {
  BlogEntry,
  RoadmapItem,
  SourceSpotlight,
  WeeklyDigestItem,
} from "@/data/blogs";

type CurationExplorerProps = {
  entries: BlogEntry[];
  roadmap: RoadmapItem[];
  sources: SourceSpotlight[];
  weeklyDigestItems: WeeklyDigestItem[];
};

export function CurationExplorer({
  entries,
  roadmap,
  sources,
  weeklyDigestItems,
}: CurationExplorerProps) {
  const [query, setQuery] = useState("");
  const [selectedTopic, setSelectedTopic] = useState("All");
  const deferredQuery = useDeferredValue(query);
  const normalizedQuery = deferredQuery.trim().toLowerCase();
  const topicFilters = ["All", ...Array.from(new Set(entries.map((entry) => entry.topic)))];
  const trackedSourceCount = new Set(entries.map((entry) => entry.source)).size;

  const filteredEntries = entries.filter((entry) => {
    const matchesTopic = selectedTopic === "All" || entry.topic === selectedTopic;
    const searchableText = [
      entry.title,
      entry.summary,
      entry.source,
      entry.topic,
      entry.highlightLabel,
    ]
      .join(" ")
      .toLowerCase();
    const matchesQuery =
      normalizedQuery.length === 0 || searchableText.includes(normalizedQuery);

    return matchesTopic && matchesQuery;
  });

  const featuredEntries = filteredEntries.filter((entry) => entry.highlight);
  const panelEntries =
    featuredEntries.length > 0 ? featuredEntries.slice(0, 2) : filteredEntries.slice(0, 2);
  const visibleEntries = filteredEntries.slice(0, 6);
  const leadEntry = panelEntries[0] ?? entries[0];
  const hasResults = visibleEntries.length > 0;

  return (
    <div className="page-shell">
      <header className="topbar">
        <a className="brand" href="#top">
          <span className="brand__mark">TH</span>
          <span>
            <strong>Techlog Hub</strong>
            <small>Next.js + Spring Boot monorepo</small>
          </span>
        </a>
        <div className="topbar__status">
          <span>Monorepo bootstrap</span>
          <span>{trackedSourceCount} sources mapped</span>
        </div>
      </header>

      <section className="hero-grid" id="top">
        <div className="hero-copy">
          <p className="eyebrow">SIGNAL OVER NOISE</p>
          <h1>기술 블로그를 매일 빠르게 훑고, 읽을 가치가 있는 글만 남긴다.</h1>
          <p className="hero-body">
            Techlog Hub는 팀이 함께 보는 기술 블로그 모음집의 첫 화면이다. 지금은
            Next.js 프론트와 Spring Boot API 골격까지 잡아두고, 실제 수집 파이프라인은
            다음 단계에서 연결한다.
          </p>
          <div className="hero-actions">
            <a className="button-primary" href="#catalog">
              큐레이션 보기
            </a>
            <a className="button-secondary" href="#roadmap">
              다음 단계
            </a>
          </div>
          <dl className="hero-stats">
            <div>
              <dt>Tracked sources</dt>
              <dd>{trackedSourceCount}</dd>
            </div>
            <div>
              <dt>Demo entries</dt>
              <dd>{entries.length}</dd>
            </div>
            <div>
              <dt>Backend shape</dt>
              <dd>Spring</dd>
            </div>
          </dl>
        </div>

        <aside className="hero-panel" aria-label="오늘의 큐레이션 초점">
          <p className="hero-panel__label">Today&apos;s editorial angle</p>
          <h2>{leadEntry.highlightLabel}</h2>
          <p className="hero-panel__summary">{leadEntry.summary}</p>
          <div className="hero-panel__meta">
            <span>{leadEntry.source}</span>
            <span>{leadEntry.readingTime}</span>
            <span>{leadEntry.freshness}</span>
          </div>
          <ul className="hero-panel__list">
            {panelEntries.map((entry) => (
              <li key={entry.id}>
                <strong>{entry.title}</strong>
                <span>{entry.topic}</span>
              </li>
            ))}
          </ul>
        </aside>
      </section>

      <section className="search-strip" aria-label="검색과 필터">
        <label className="search-field">
          <span>Search feed</span>
          <input
            type="search"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="React, AI, observability 같은 주제로 검색"
          />
        </label>
        <p className="result-caption">
          {selectedTopic === "All" ? "All topics" : selectedTopic} / {filteredEntries.length}
          개의 결과
        </p>
      </section>

      <section className="topic-strip" aria-label="주제 필터">
        {topicFilters.map((topic) => {
          const isActive = topic === selectedTopic;

          return (
            <button
              key={topic}
              type="button"
              className={`topic-chip${isActive ? " is-active" : ""}`}
              onClick={() => setSelectedTopic(topic)}
            >
              {topic}
            </button>
          );
        })}
      </section>

      <section className="workspace-grid">
        <div className="catalog-column" id="catalog">
          <div className="section-head">
            <div>
              <p className="section-kicker">Curated queue</p>
              <h2>이번 주에 읽어둘 만한 글</h2>
            </div>
            <p className="section-note">검색과 토픽 선택에 따라 목록이 즉시 좁혀진다.</p>
          </div>
          {hasResults ? (
            <div className="catalog-grid">
              {visibleEntries.map((entry) => (
                <PostCard entry={entry} key={entry.id} />
              ))}
            </div>
          ) : (
            <div className="result-empty">
              <h3>조건에 맞는 글이 없다.</h3>
              <p>
                검색어를 줄이거나 토픽 필터를 다시 선택하면 데모 큐레이션이 다시 보인다.
              </p>
            </div>
          )}
        </div>

        <aside className="sidebar">
          <section className="sidebar-block">
            <p className="section-kicker">Source board</p>
            <h2>모니터링 중인 채널</h2>
            <ul className="source-list">
              {sources.map((source) => (
                <li key={source.name}>
                  <div>
                    <strong>{source.name}</strong>
                    <p>{source.focus}</p>
                  </div>
                  <span>{source.cadence}</span>
                </li>
              ))}
            </ul>
          </section>

          <section className="sidebar-block">
            <p className="section-kicker">Weekly digest</p>
            <h2>큐레이터 메모</h2>
            <ul className="digest-list">
              {weeklyDigestItems.map((item) => (
                <li key={item.title}>
                  <strong>{item.title}</strong>
                  <p>{item.note}</p>
                </li>
              ))}
            </ul>
          </section>
        </aside>
      </section>

      <section className="roadmap-band" id="roadmap">
        <div className="section-head">
          <div>
            <p className="section-kicker">Next build</p>
            <h2>이 데모 다음에 붙일 것들</h2>
          </div>
          <p className="section-note">
            정적 목록에서 끝내지 않고 수집, 저장, 개인화 단계로 확장한다.
          </p>
        </div>
        <div className="roadmap-grid">
          {roadmap.map((item) => (
            <article className="roadmap-step" key={item.step}>
              <p>{item.step}</p>
              <h3>{item.title}</h3>
              <span>{item.description}</span>
            </article>
          ))}
        </div>
      </section>
    </div>
  );
}
