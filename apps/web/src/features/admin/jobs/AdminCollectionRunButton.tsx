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
  ADMIN_BATCH_JOB_NOT_FOUND: "작업 실행 정보를 찾지 못했습니다. 작업 목록을 새로고침해 주세요.",
  ADMIN_SESSION_INVALID: "관리자 세션이 만료되었습니다. 다시 로그인해 주세요.",
};

const terminalJobStatuses = new Set<AdminJobRunDto["status"]>([
  "completed",
  "failed",
  "stopped",
  "abandoned",
]);

const jobStatusPollIntervalMs = 1_000;
const jobStatusPollMaxAttempts = 30;

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
    setResult(null);

    startTransition(async () => {
      try {
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

        const acceptedResult = payload as AdminJobRunDto;
        setResult(acceptedResult);
        setResult(await pollJobExecution(acceptedResult));
        router.refresh();
      } catch {
        setErrorMessage("네트워크 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
      }
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

async function pollJobExecution(initialResult: AdminJobRunDto): Promise<AdminJobRunDto> {
  let latestResult = initialResult;

  for (let attempt = 0; attempt < jobStatusPollMaxAttempts; attempt++) {
    if (terminalJobStatuses.has(latestResult.status)) {
      return latestResult;
    }

    await delay(jobStatusPollIntervalMs);
    const response = await fetch(`/admin/api/jobs/executions/${latestResult.executionId}`, {
      cache: "no-store",
    });

    if (!response.ok) {
      return latestResult;
    }

    latestResult = (await response.json()) as AdminJobRunDto;
  }

  return latestResult;
}

function delay(ms: number) {
  return new Promise((resolve) => {
    setTimeout(resolve, ms);
  });
}
