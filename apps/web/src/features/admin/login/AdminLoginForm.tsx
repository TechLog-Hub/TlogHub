"use client";

import { useState, useTransition } from "react";

import { Button } from "@/shared/ui/Button";
import { Input } from "@/shared/ui/Input";

type LoginError = {
  email?: string;
  password?: string;
  form?: string;
};

const loginErrorMessages: Record<string, string> = {
  ADMIN_INVALID_CREDENTIALS: "이메일 또는 비밀번호가 올바르지 않습니다.",
  ADMIN_LOGIN_LOCKED: "로그인이 잠시 제한되었습니다. 잠시 후 다시 시도해 주세요.",
};

export function AdminLoginForm() {
  const [error, setError] = useState<LoginError>({});
  const [isPending, startTransition] = useTransition();

  function handleSubmit(formData: FormData) {
    const email = String(formData.get("email") ?? "").trim();
    const password = String(formData.get("password") ?? "");
    const nextError: LoginError = {};

    if (!email) {
      nextError.email = "이메일을 입력해 주세요.";
    }

    if (!password) {
      nextError.password = "비밀번호를 입력해 주세요.";
    }

    setError(nextError);

    if (nextError.email || nextError.password) {
      return;
    }

    startTransition(async () => {
      const response = await fetch("/api/admin/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, password }),
      });

      if (response.ok) {
        window.location.assign("/admin");
        return;
      }

      const payload = await response.json().catch(() => ({ code: "UNKNOWN_ERROR" }));
      setError({
        form: loginErrorMessages[payload.code as string] ?? "일시적으로 로그인할 수 없습니다.",
      });
    });
  }

  return (
    <form className="login-card" action={handleSubmit}>
      <div>
        <p className="login-card__eyebrow">운영 콘솔</p>
        <h1>T-Log Admin</h1>
        <p className="login-card__description">수집 소스와 글 상태를 확인합니다.</p>
      </div>
      <Input
        label="이메일"
        name="email"
        type="email"
        autoComplete="username"
        disabled={isPending}
        error={error.email}
      />
      <Input
        label="비밀번호"
        name="password"
        type="password"
        autoComplete="current-password"
        disabled={isPending}
        error={error.password}
      />
      {error.form ? <p className="login-card__error">{error.form}</p> : null}
      <Button type="submit" disabled={isPending}>
        {isPending ? "로그인 중" : "로그인"}
      </Button>
    </form>
  );
}
