import type { PublicPostListItemDto } from "@/features/public/types/public-api.dto";

const dateFormatter = new Intl.DateTimeFormat("ko-KR", {
  year: "numeric",
  month: "2-digit",
  day: "2-digit",
});

export function formatPostDate(value: string): string {
  return dateFormatter.format(new Date(value)).replaceAll(". ", ".").replace(/\.$/, "");
}

export function isNewPost(value: string): boolean {
  const publishedAt = new Date(value).getTime();
  return Number.isFinite(publishedAt) && Date.now() - publishedAt <= 24 * 60 * 60 * 1000;
}

export function companyInitials(post: PublicPostListItemDto): string {
  return post.company.name.slice(0, 2);
}

export function publicSummaryText(summaryState: string, preview?: string): string {
  if (summaryState === "ready" && preview) {
    return preview;
  }

  return "AI 요약을 준비하고 있어요. 원문은 바로 확인할 수 있습니다.";
}
