import { getPublicFilters, getPublicPosts } from "@/features/public/api/public-posts.api";
import { PublicPostFeed, PublicSidebar } from "@/features/public/components/PublicPostFeed";
import { PublicSearchBox } from "@/features/public/components/PublicSearchBox";
import { PublicShell } from "@/features/public/components/PublicShell";
import { PublicErrorState } from "@/features/public/components/PublicStates";
import type {
  PageResponse,
  PublicFilterMetadataDto,
  PublicPostListItemDto,
  PublicPostSearchQuery,
} from "@/features/public/types/public-api.dto";
import { parsePublicSearchParams, type RawSearchParams } from "@/features/public/utils/public-routing";

type HomePageProps = {
  searchParams: Promise<RawSearchParams>;
};

export default async function HomePage({ searchParams }: HomePageProps) {
  const query = parsePublicSearchParams(await searchParams);
  const data = await loadHomeData(query);

  return (
    <PublicShell active="archive">
      <main>
        {"error" in data ? (
          <div className="home-content-grid home-content-grid--single">
            <PublicErrorState />
          </div>
        ) : (
          <>
            <PublicSearchBox query={query} />
            <div className="home-content-grid">
              <PublicPostFeed posts={data.posts} filters={data.filters} query={query} />
              <div className="desktop-side-panels">
                <PublicSidebar posts={data.posts} filters={data.filters} />
              </div>
            </div>
          </>
        )}
      </main>
    </PublicShell>
  );
}

async function loadHomeData(query: PublicPostSearchQuery): Promise<
  | {
      posts: PageResponse<PublicPostListItemDto>;
      filters: PublicFilterMetadataDto;
    }
  | { error: true }
> {
  try {
    const [posts, filters] = await Promise.all([getPublicPosts(query), getPublicFilters()]);
    return { posts, filters };
  } catch {
    return { error: true };
  }
}
