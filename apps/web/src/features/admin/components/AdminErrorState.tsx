type AdminErrorStateProps = {
  title?: string;
  description?: string;
};

export function AdminErrorState({
  title = "화면을 불러오지 못했습니다.",
  description = "잠시 후 다시 시도해 주세요.",
}: AdminErrorStateProps) {
  return (
    <section className="error-state">
      <strong>{title}</strong>
      <p>{description}</p>
    </section>
  );
}
