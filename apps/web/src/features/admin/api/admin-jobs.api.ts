import "server-only";

import { adminFetch } from "@/features/admin/api/admin-fetch";
import type {
  AdminFailureDto,
  AdminJobRunDto,
} from "@/features/admin/types/admin-api.dto";

type RunCollectionJobParams = {
  sourceId?: number;
  reason?: string;
};

export async function runAdminCollectionJob(params: RunCollectionJobParams = {}) {
	return adminFetch<AdminJobRunDto>("/api/v1/admin/jobs/collect/run", {
		method: "POST",
		query: {
			sourceId: params.sourceId,
      reason: params.reason,
    },
	});
}

export async function getAdminJobExecution(executionId: number) {
	return adminFetch<AdminJobRunDto>(`/api/v1/admin/jobs/executions/${executionId}`);
}

export async function getAdminCollectionFailures(size = 20) {
	return adminFetch<AdminFailureDto[]>("/api/v1/admin/jobs/failures", {
    query: { size },
  });
}
