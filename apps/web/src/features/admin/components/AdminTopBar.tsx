"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";

import type { AdminMeDto } from "@/features/admin/types/admin-api.dto";
import { Button } from "@/shared/ui/Button";

type AdminTopBarProps = {
  admin: AdminMeDto;
};

export function AdminTopBar({ admin }: AdminTopBarProps) {
  const router = useRouter();
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  async function handleLogout() {
    setIsLoggingOut(true);
    await fetch("/admin/api/auth/logout", { method: "POST" }).catch(() => undefined);
    router.replace("/admin/login");
    router.refresh();
  }

  return (
    <header className="admin-topbar">
      <div>
        <strong className="admin-topbar__brand">T-Log Admin</strong>
        <span className="admin-topbar__env">{process.env.NODE_ENV === "production" ? "Production" : "Local"}</span>
      </div>
      <div className="admin-topbar__right">
        <span className="admin-topbar__email">{admin.email}</span>
        <Button type="button" variant="ghost" onClick={handleLogout} disabled={isLoggingOut}>
          로그아웃
        </Button>
      </div>
    </header>
  );
}
