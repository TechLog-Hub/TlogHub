"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const navItems = [
  { label: "홈", href: "/admin" },
  { label: "소스", href: "/admin/sources" },
  { label: "작업", href: "/admin/jobs" },
  { label: "글", href: "/admin/posts" },
];

export function AdminSideNav() {
  const pathname = usePathname();

  return (
    <aside className="admin-sidenav" aria-label="관리자 메뉴">
      <nav>
        {navItems.map((item) => {
          const isActive = pathname === item.href || (item.href !== "/admin" && pathname.startsWith(item.href));

          return (
            <Link key={item.href} className={`admin-sidenav__link ${isActive ? "is-active" : ""}`} href={item.href}>
              {item.label}
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}
