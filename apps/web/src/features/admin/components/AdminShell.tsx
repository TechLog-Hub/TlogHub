import type { ReactNode } from "react";

import { AdminSideNav } from "@/features/admin/components/AdminSideNav";
import { AdminTopBar } from "@/features/admin/components/AdminTopBar";
import type { AdminMeDto } from "@/features/admin/types/admin-api.dto";

type AdminShellProps = {
  admin: AdminMeDto;
  children: ReactNode;
};

export function AdminShell({ admin, children }: AdminShellProps) {
  return (
    <div className="admin-shell">
      <AdminTopBar admin={admin} />
      <div className="admin-shell__body">
        <AdminSideNav />
        <main className="admin-shell__content">{children}</main>
      </div>
    </div>
  );
}
