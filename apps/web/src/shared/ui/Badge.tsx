import type { AdminStatusTone } from "@/features/admin/types/admin-view.model";

type BadgeProps = {
  label: string;
  tone?: AdminStatusTone;
};

export function Badge({ label, tone = "neutral" }: BadgeProps) {
  return <span className={`badge badge--${tone}`}>{label}</span>;
}
