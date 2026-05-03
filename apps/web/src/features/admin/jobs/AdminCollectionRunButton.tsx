"use client";

import { useRouter } from "next/navigation";
import { useState, useTransition } from "react";

import { AdminStatusBadge } from "@/features/admin/components/AdminStatusBadge";
import type { AdminJobRunDto } from "@/features/admin/types/admin-api.dto";
import { toAdminJobRunResult } from "@/features/admin/utils/admin-view-mapper";
import { Button } from "@/shared/ui/Button";

type AdminCollectionRunButtonProps = {
  sourceId?: number;
  label?: string;
  reason?: string;
  disabled?: boolean;
  disabledReason?: string;
  variant?: "primary" | "secondary" | "ghost";
  compact?: boolean;
};

const jobRunErrorMessages: Record<string, string> = {
  ADMIN_BATCH_JOB_ALREADY_RUNNING: "이미 수집 작업이 실행 중입니다. 잠시 후 다시 시도해 주세요.",
  ADMIN_BATCH_JOB_LAUNCH_FAILED: "수집 작업을 시작하지 못했습니다. 서버 상태를 확인해 주세요.",
  ADMIN_SESSION_INVALID: "관리자 세션이 만료되었습니다. 다시 로그인해 주세요.",
};

export function AdminCollectionRunButton({
  sourceId,
  label = "수집 실행",
  reason = "manual-admin-ui",
  disabled = false,
  disabledReason,
  variant = "primary",
  compact = false,
}: AdminCollectionRunButtonProps) {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();
  const [result, setResult] = useState<AdminJobRunDto | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const isDisabled = disabled || isPending;

  function handleRun() {
    setErrorMessage(null);

    startTransition(async () => {
      const response = await fetch("/admin/api/jobs/collect/run", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ sourceId, reason }),
      });

      const payload = await response.json().catch(() => ({ code: "UNKNOWN_ERROR" }));

      if (!response.ok) {
        const code = typeof payload.code === "string" ? payload.code : "UNKNOWN_ERROR";
        setErrorMessage(jobRunErrorMessages[code] ?? "수집 작업을 실행하지 못했습니다.");
        return;
      }

      setResult(payload as AdminJobRunDto);
      router.refresh();
    });
  }

  const runResult = result ? toAdminJobRunResult(result) : null;

  return (
    <div className={`job-run ${compact ? "job-run--compact" : ""}`}>
      <Button
        type="button"
        variant={variant}
        disabled={isDisabled}
        title={disabled ? disabledReason : undefined}
        onClick={handleRun}
      >
        {isPending ? "실행 중" : label}
      </Button>
      {disabled && disabledReason && !compact ? <p className="job-run__hint">{disabledReason}</p> : null}
      {runResult ? (
        <div className="job-run__feedback" role="status">
          <span>실행 {runResult.executionLabel}</span>
          <AdminStatusBadge status={runResult.status} />
          <span>{runResult.requestedAtLabel}</span>
        </div>
      ) : null}
      {errorMessage ? (
        <p className="job-run__error" role="alert">
          {errorMessage}
        </p>
      ) : null}
    </div>
  );
}
