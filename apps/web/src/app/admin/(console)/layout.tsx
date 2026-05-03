import type { ReactNode } from "react";

import { requireAdminSession } from "@/features/admin/api/admin-auth.api";
import { AdminShell } from "@/features/admin/components/AdminShell";

export const dynamic = "force-dynamic";

export default async function AdminConsoleLayout({ children }: { children: ReactNode }) {
  const admin = await requireAdminSession();

  return <AdminShell admin={admin}>{children}</AdminShell>;
}
