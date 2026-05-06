import { getPublicFilters } from "@/features/public/api/public-posts.api";
import { PublicShell } from "@/features/public/components/PublicShell";
import { SubscribeForm } from "@/features/public/components/SubscribeForm";

type SubscribePageProps = {
  searchParams: Promise<{ company?: string }>;
};

export default async function SubscribePage({ searchParams }: SubscribePageProps) {
  const [{ company }, filters] = await Promise.all([searchParams, getPublicFilters()]);

  return (
    <PublicShell active="subscribe">
      <main className="subscribe-page">
        <div className="subscribe-page__head">
          <h1>새 글 알림을 가볍게 받습니다.</h1>
          <p>이메일과 관심 기업만 선택하면 새 글의 AI 요약과 원문 링크를 받아볼 수 있습니다.</p>
        </div>
        <SubscribeForm companies={filters.companies} initialCompany={company} />
      </main>
    </PublicShell>
  );
}
