import { redirect } from "next/navigation";

import { getOptionalAdminMe } from "@/features/admin/api/admin-auth.api";
import { AdminLoginForm } from "@/features/admin/login/AdminLoginForm";

export const dynamic = "force-dynamic";

export default async function AdminLoginPage() {
  const admin = await getOptionalAdminMe();

  if (admin) {
    redirect("/admin");
  }

  return (
    <main className="login-page">
      <AdminLoginForm />
    </main>
  );
}
