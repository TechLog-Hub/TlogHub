import { notFound } from "next/navigation";

import { getAdminPostDetail } from "@/features/admin/api/admin-posts.api";
import { AdminPostDetailPanel } from "@/features/admin/posts/AdminPostDetailPanel";
import { AppApiError } from "@/shared/api/api-error";

export const dynamic = "force-dynamic";

type AdminPostDetailPageProps = {
  params: Promise<{
    postId: string;
  }>;
};

export default async function AdminPostDetailPage({ params }: AdminPostDetailPageProps) {
  const { postId } = await params;
  const post = await findAdminPostDetail(postId);

  return (
    <div className="page-stack">
      <AdminPostDetailPanel post={post} />
    </div>
  );
}

async function findAdminPostDetail(postId: string) {
  try {
    return await getAdminPostDetail(postId);
  } catch (error) {
    if (error instanceof AppApiError && error.status === 404) {
      notFound();
    }

    throw error;
  }
}
