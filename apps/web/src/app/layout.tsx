import type { Metadata } from "next";
import localFont from "next/font/local";
import type { ReactNode } from "react";

import "./globals.css";

const pretendard = localFont({
  src: [
    { path: "./fonts/Pretendard-Regular.woff", weight: "400", style: "normal" },
    { path: "./fonts/Pretendard-Medium.woff", weight: "500", style: "normal" },
    { path: "./fonts/Pretendard-SemiBold.woff", weight: "600", style: "normal" },
    { path: "./fonts/Pretendard-Bold.woff", weight: "700", style: "normal" },
    { path: "./fonts/Pretendard-ExtraBold.woff", weight: "800", style: "normal" },
  ],
  variable: "--font-pretendard",
  display: "swap",
});

export const metadata: Metadata = {
  title: "Techlog Hub",
  description: "기술 블로그 아카이브 서비스",
};

export default function RootLayout({ children }: Readonly<{ children: ReactNode }>) {
  return (
    <html lang="ko" className={pretendard.variable}>
      <body>{children}</body>
    </html>
  );
}
