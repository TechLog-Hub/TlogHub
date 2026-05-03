const fullDateTimeFormatter = new Intl.DateTimeFormat("ko-KR", {
  year: "numeric",
  month: "2-digit",
  day: "2-digit",
  hour: "2-digit",
  minute: "2-digit",
  hour12: false,
});

const monthDateTimeFormatter = new Intl.DateTimeFormat("ko-KR", {
  month: "2-digit",
  day: "2-digit",
  hour: "2-digit",
  minute: "2-digit",
  hour12: false,
});

const timeFormatter = new Intl.DateTimeFormat("ko-KR", {
  hour: "2-digit",
  minute: "2-digit",
  hour12: false,
});

export function formatAdminDateTime(value: string | null | undefined, emptyLabel = "없음"): string {
  if (!value) {
    return emptyLabel;
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return emptyLabel;
  }

  const now = new Date();
  const isSameDay =
    now.getFullYear() === date.getFullYear() &&
    now.getMonth() === date.getMonth() &&
    now.getDate() === date.getDate();

  if (isSameDay) {
    return `오늘 ${timeFormatter.format(date)}`;
  }

  if (now.getFullYear() === date.getFullYear()) {
    return monthDateTimeFormatter.format(date);
  }

  return fullDateTimeFormatter.format(date);
}

export function formatAdminDate(value: string | null | undefined, emptyLabel = "없음"): string {
  const formatted = formatAdminDateTime(value, emptyLabel);
  return formatted === emptyLabel ? formatted : formatted.replace(/\s\d{2}:\d{2}$/, "");
}
