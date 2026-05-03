import Link from "next/link";

export default function AdminPostNotFoundPage() {
  return (
    <section className="empty-state">
      <strong>글을 찾을 수 없습니다.</strong>
      <p>삭제되었거나 접근할 수 없는 글입니다.</p>
      <Link className="button button--secondary" href="/admin/posts">
        목록으로
      </Link>
    </section>
  );
}
