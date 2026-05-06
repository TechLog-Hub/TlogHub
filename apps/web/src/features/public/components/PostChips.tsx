import Link from "next/link";

const jobClassMap: Record<string, string> = {
  Frontend: "job-frontend",
  Backend: "job-backend",
  Data: "job-data",
  Mobile: "job-mobile",
  Infra: "job-infra",
};

export function JobChip({ job, href }: { job: string; href?: string }) {
  const className = `job-chip ${jobClassMap[job] ?? "job-default"}`;
  if (href) {
    return (
      <Link href={href} className={className}>
        {job}
      </Link>
    );
  }

  return <span className={className}>{job}</span>;
}

export function TagChip({ tag, href }: { tag: string; href?: string }) {
  if (href) {
    return (
      <Link href={href} className="tag-chip">
        {tag}
      </Link>
    );
  }

  return <span className="tag-chip">{tag}</span>;
}
