import { CurationExplorer } from "@/components/curation-explorer";
import {
  blogEntries,
  deliveryRoadmap,
  sourceSpotlights,
  weeklyDigest,
} from "@/data/blogs";

export default function Home() {
  return (
    <main>
      <CurationExplorer
        entries={blogEntries}
        roadmap={deliveryRoadmap}
        sources={sourceSpotlights}
        weeklyDigestItems={weeklyDigest}
      />
    </main>
  );
}
