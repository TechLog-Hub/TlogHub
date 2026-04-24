export type BlogEntry = {
  id: string;
  title: string;
  source: string;
  topic: string;
  summary: string;
  readingTime: string;
  freshness: string;
  url: string;
  highlight: boolean;
  highlightLabel: string;
};

export type SourceSpotlight = {
  name: string;
  focus: string;
  cadence: string;
};

export type WeeklyDigestItem = {
  title: string;
  note: string;
};

export type RoadmapItem = {
  step: string;
  title: string;
  description: string;
};

export const blogEntries: BlogEntry[] = [
  {
    id: "react-compiler-watch",
    title: "React Compiler 도입 전에 렌더 규칙부터 정리하기",
    source: "React Blog",
    topic: "Frontend",
    summary:
      "성능 팁을 모으기보다 컴포넌트 순수성, 상태 소유권, Effect 사용 이유를 먼저 정리해야 한다는 흐름을 담은 데모 항목이다.",
    readingTime: "8 min read",
    freshness: "Today",
    url: "https://react.dev/blog",
    highlight: true,
    highlightLabel: "프론트엔드 성능 변화는 문법보다 규칙에서 시작된다.",
  },
  {
    id: "vercel-observability",
    title: "Preview 배포와 observability를 한 흐름으로 보는 이유",
    source: "Vercel",
    topic: "Frontend",
    summary:
      "프리뷰 환경, 배포 속도, 에러 추적을 한 번에 보는 운영 관점이 왜 필요한지 보여주는 데모 큐레이션이다.",
    readingTime: "6 min read",
    freshness: "1 day ago",
    url: "https://vercel.com/blog",
    highlight: false,
    highlightLabel: "프리뷰 링크는 배포가 아니라 리뷰 속도를 바꾸는 장치다.",
  },
  {
    id: "spring-runtime-signals",
    title: "Spring 서비스의 운영 신호를 로그보다 메트릭에서 읽기",
    source: "Spring",
    topic: "Backend",
    summary:
      "백엔드 블로그를 모을 때는 신규 기능보다 트래픽과 장애 대응에 직접 연결되는 관측 가능성 글을 우선으로 본다.",
    readingTime: "9 min read",
    freshness: "2 days ago",
    url: "https://spring.io/blog",
    highlight: true,
    highlightLabel: "운영 품질은 서비스 규모보다 관측 습관에서 갈린다.",
  },
  {
    id: "openai-evals-patterns",
    title: "에이전트 기능을 붙이기 전에 평가 루프부터 만들기",
    source: "OpenAI",
    topic: "AI",
    summary:
      "AI 관련 글은 모델 기능보다 평가 체계, 비용 추적, 실패 유형 정리에 초점을 맞춰 모으는 방향을 예시로 보여준다.",
    readingTime: "7 min read",
    freshness: "3 days ago",
    url: "https://openai.com/",
    highlight: false,
    highlightLabel: "AI 기능의 품질은 프롬프트보다 평가 루프에서 안정된다.",
  },
  {
    id: "cloudflare-edge-cache",
    title: "Edge 캐시 전략을 제품 경험으로 연결하는 방법",
    source: "Cloudflare",
    topic: "Cloud",
    summary:
      "플랫폼 블로그는 속도 자체보다 사용자 체감 성능과 운영 비용을 같이 설명하는 글을 우선으로 큐레이션한다.",
    readingTime: "5 min read",
    freshness: "4 days ago",
    url: "https://blog.cloudflare.com/",
    highlight: false,
    highlightLabel: "캐시는 인프라 기능이 아니라 제품 경험의 일부다.",
  },
  {
    id: "duckdb-local-analytics",
    title: "로컬 분석 도구를 붙여 기술 블로그 트렌드를 빠르게 보는 법",
    source: "DuckDB",
    topic: "Data",
    summary:
      "모아둔 글의 태그와 빈도를 바로 탐색하기 위한 데이터 분석 레이어 후보를 설명하는 데모 항목이다.",
    readingTime: "6 min read",
    freshness: "5 days ago",
    url: "https://duckdb.org/",
    highlight: false,
    highlightLabel: "큐레이션의 다음 단계는 저장보다 탐색 경험이다.",
  },
  {
    id: "github-actions-quality",
    title: "GitHub Actions를 품질 게이트로 쓸 때 놓치기 쉬운 것들",
    source: "GitHub",
    topic: "DevOps",
    summary:
      "개발 생산성 글은 도구 사용법보다 팀 워크플로우와 리뷰 루프를 개선하는 내용 위주로 모으는 것이 효율적이다.",
    readingTime: "7 min read",
    freshness: "6 days ago",
    url: "https://github.blog/",
    highlight: false,
    highlightLabel: "자동화는 스크립트 수보다 피드백 시간을 줄여야 의미가 있다.",
  },
  {
    id: "kubernetes-release-ops",
    title: "쿠버네티스 릴리스를 운영팀 관점에서 읽는 체크포인트",
    source: "Kubernetes",
    topic: "Cloud",
    summary:
      "릴리스 노트를 단순 뉴스가 아니라 업그레이드 위험과 비용 변화 관점으로 추려내는 예시를 담은 카드다.",
    readingTime: "10 min read",
    freshness: "1 week ago",
    url: "https://kubernetes.io/blog/",
    highlight: false,
    highlightLabel: "릴리스 노트는 기능 목록이 아니라 운영 리스크 문서다.",
  },
];

export const sourceSpotlights: SourceSpotlight[] = [
  {
    name: "React Blog",
    focus: "렌더링 모델, 컴포넌트 규칙, 프론트엔드 런타임 변화",
    cadence: "Daily",
  },
  {
    name: "Spring Blog",
    focus: "백엔드 아키텍처, 운영 신호, 통합 패턴",
    cadence: "Tue / Thu",
  },
  {
    name: "Cloudflare Blog",
    focus: "Edge 런타임, 캐시, 네트워크 레이어 변화",
    cadence: "Daily",
  },
  {
    name: "GitHub Blog",
    focus: "개발 생산성, 협업 워크플로우, CI/CD 습관",
    cadence: "Weekly",
  },
];

export const weeklyDigest: WeeklyDigestItem[] = [
  {
    title: "성능 글의 무게중심",
    note: "최근 글들은 미세 최적화보다 렌더 규칙, 캐시, 관측 가능성으로 중심이 이동했다.",
  },
  {
    title: "AI 글의 선별 기준",
    note: "데모 발표보다 평가 체계와 비용 제어를 설명하는 글이 재사용 가치가 높다.",
  },
  {
    title: "플랫폼 글의 체크포인트",
    note: "배포 속도보다 프리뷰, 롤백, 캐시 무효화 같은 운영 흐름을 함께 보는 편이 좋다.",
  },
];

export const deliveryRoadmap: RoadmapItem[] = [
  {
    step: "01",
    title: "RSS 수집기",
    description: "등록한 기술 블로그를 주기적으로 읽고 최신 글 메타데이터를 저장한다.",
  },
  {
    step: "02",
    title: "태그와 저장",
    description: "카테고리, 난이도, 팀 관심도 같은 메타 태그를 붙여 큐레이션 품질을 높인다.",
  },
  {
    step: "03",
    title: "개인화 다이제스트",
    description: "사용자별로 읽을 가치가 높은 글만 묶어 메일이나 슬랙 요약으로 보낸다.",
  },
];
