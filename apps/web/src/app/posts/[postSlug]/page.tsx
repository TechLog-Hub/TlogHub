import { notFound } from "next/navigation";

import { getPublicPost, getPublicPosts } from "@/features/public/api/public-posts.api";
import { PostDetailView } from "@/features/public/components/PostDetailView";
import { PublicShell } from "@/features/public/components/PublicShell";
import type { PublicPostDetailDto, PublicPostListItemDto } from "@/features/public/types/public-api.dto";
import { AppApiError } from "@/shared/api/api-error";

type PostDetailPageProps = {
  params: Promise<{ postSlug: string }>;
};

export default async function PostDetailPage({ params }: PostDetailPageProps) {
  const { postSlug } = await params;
  const data = await loadPostDetailData(postSlug);

  if (!data) {
    notFound();
  }

  return (
    <PublicShell active="archive">
      <PostDetailView post={data.post} relatedPosts={data.relatedPosts} />
    </PublicShell>
  );
}

async function loadPostDetailData(
  postSlug: string,
): Promise<{ post: PublicPostDetailDto; relatedPosts: PublicPostListItemDto[] } | null> {
  try {
    const post = await getPublicPost(postSlug);
    const relatedPage = await getPublicPosts({
      company: [post.company.slug],
      size: 4,
      sort: "latest",
    });
    const relatedPosts = relatedPage.content.filter((item) => item.slug !== post.slug).slice(0, 3);
    return { post, relatedPosts };
  } catch (error) {
    if (error instanceof AppApiError && error.status === 404) {
      return null;
    }

    throw error;
  }
}
