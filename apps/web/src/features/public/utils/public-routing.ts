import type { PublicPostSearchQuery, PublicPostSort } from "@/features/public/types/public-api.dto";

export type RawSearchParams = Record<string, string | string[] | undefined>;

const DEFAULT_PAGE_SIZE = 12;

export function parsePublicSearchParams(searchParams: RawSearchParams): PublicPostSearchQuery {
  const q = first(searchParams.q);
  const sort = parseSort(first(searchParams.sort), q);
  const page = Math.max(parsePositiveInt(first(searchParams.page), 1) - 1, 0);

  return {
    q,
    company: all(searchParams.company),
    job: all(searchParams.job),
    tag: all(searchParams.tag),
    sort,
    page,
    size: DEFAULT_PAGE_SIZE,
  };
}

export function toSearchHref(pathname: string, query: PublicPostSearchQuery, patch: Partial<PublicPostSearchQuery>): string {
  const next: PublicPostSearchQuery = {
    ...query,
    ...patch,
    page: patch.page ?? 0,
  };
  const params = new URLSearchParams();

  append(params, "q", next.q);
  append(params, "sort", next.sort);
  next.company?.forEach((value) => append(params, "company", value));
  next.job?.forEach((value) => append(params, "job", value));
  next.tag?.forEach((value) => append(params, "tag", value));

  if (next.page && next.page > 0) {
    params.set("page", String(next.page + 1));
  }

  const queryString = params.toString();
  return queryString ? `${pathname}?${queryString}` : pathname;
}

export function filterToggleHref(
  pathname: string,
  query: PublicPostSearchQuery,
  key: "company" | "job" | "tag",
  value: string,
): string {
  const currentValues = query[key] ?? [];
  const values = currentValues.includes(value)
    ? currentValues.filter((item) => item !== value)
    : [...currentValues, value];
  return toSearchHref(pathname, query, { [key]: values });
}

export function isSelected(values: string[] | undefined, value: string): boolean {
  return (values ?? []).includes(value);
}

function first(value: string | string[] | undefined): string | undefined {
  return Array.isArray(value) ? value[0] : value;
}

function all(value: string | string[] | undefined): string[] {
  if (!value) {
    return [];
  }
  const values = Array.isArray(value) ? value : [value];
  return values.map((item) => item.trim()).filter(Boolean);
}

function parseSort(value: string | undefined, q: string | undefined): PublicPostSort {
  if (value === "latest" || value === "relevance") {
    return value;
  }
  return q ? "relevance" : "latest";
}

function parsePositiveInt(value: string | undefined, fallback: number): number {
  const parsed = Number.parseInt(value ?? "", 10);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
}

function append(params: URLSearchParams, key: string, value: string | undefined): void {
  if (value && value.trim()) {
    params.append(key, value);
  }
}
