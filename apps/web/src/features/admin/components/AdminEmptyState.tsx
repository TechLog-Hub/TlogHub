import Link from "next/link";

type AdminEmptyStateProps = {
  title: string;
  description: string;
  resetHref?: string;
};

export function AdminEmptyState({ title, description, resetHref }: AdminEmptyStateProps) {
  return (
    <section className="empty-state">
      <strong>{title}</strong>
      <p>{description}</p>
      {resetHref ? (
        <Link className="button button--secondary" href={resetHref}>
          필터 초기화
        </Link>
      ) : null}
    </section>
  );
}
