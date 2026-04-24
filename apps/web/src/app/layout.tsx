import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Techlog Hub",
  description: "기술 블로그를 모아 보고 검색과 필터로 빠르게 큐레이션하는 데모 사이트",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}
