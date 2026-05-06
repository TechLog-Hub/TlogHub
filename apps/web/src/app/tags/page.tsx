import { getPublicFilters } from "@/features/public/api/public-posts.api";
import { PublicShell } from "@/features/public/components/PublicShell";
import { TagDirectory } from "@/features/public/components/SimpleDirectoryPages";

export default async function TagsPage() {
  const filters = await getPublicFilters();

  return (
    <PublicShell active="tags">
      <TagDirectory tags={filters.tags} />
    </PublicShell>
  );
}
