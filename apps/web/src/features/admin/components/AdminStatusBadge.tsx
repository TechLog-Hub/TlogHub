import type { AdminStatusBadgeModel } from "@/features/admin/types/admin-view.model";
import { Badge } from "@/shared/ui/Badge";

type AdminStatusBadgeProps = {
  status: AdminStatusBadgeModel;
};

export function AdminStatusBadge({ status }: AdminStatusBadgeProps) {
  return <Badge label={status.label} tone={status.tone} />;
}
