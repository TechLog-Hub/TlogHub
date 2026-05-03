"use client";

import { AdminErrorState } from "@/features/admin/components/AdminErrorState";
import { Button } from "@/shared/ui/Button";

export default function AdminConsoleError({ reset }: { reset: () => void }) {
  return (
    <div className="page-stack">
      <AdminErrorState />
      <Button type="button" variant="secondary" onClick={reset}>
        다시 시도
      </Button>
    </div>
  );
}
