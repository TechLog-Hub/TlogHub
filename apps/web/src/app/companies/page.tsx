import { getPublicFilters } from "@/features/public/api/public-posts.api";
import { CompanyDirectory } from "@/features/public/components/SimpleDirectoryPages";
import { PublicShell } from "@/features/public/components/PublicShell";

export default async function CompaniesPage() {
  const filters = await getPublicFilters();

  return (
    <PublicShell active="companies">
      <CompanyDirectory companies={filters.companies} />
    </PublicShell>
  );
}
