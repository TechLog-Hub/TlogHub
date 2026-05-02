# T-Log

T-Log는 여러 기업의 기술 블로그 글을 한곳에 모아 탐색할 수 있게 만드는 기술 콘텐츠 아카이브 서비스다.

사용자는 여러 기업 블로그를 직접 돌아다니지 않아도 최신 기술 글을 확인하고, 기업·직군·주제 태그를 조합해 원하는 글만 빠르게 찾을 수 있다. 글 상세에서는 원문 전문을 재배포하지 않고 AI 요약과 출처 정보를 제공해, 사용자가 원문을 열기 전에 어떤 글인지 판단할 수 있도록 돕는다.

## 해결하려는 문제

기업 기술 블로그는 채용, 기술 브랜딩, 아키텍처 의사결정, 장애 대응, 운영 경험을 이해하는 데 가치가 높다. 그러나 글이 각 기업 사이트에 흩어져 있고, 제목만으로는 읽을 만한 글인지 판단하기 어렵다.

T-Log는 이 문제를 다음 방식으로 해결한다.

- 승인된 기업 기술 블로그 소스를 수집한다.
- 수집한 글을 기업, 직군, 주제 태그 기준으로 분류한다.
- AI 요약으로 글의 핵심 내용을 빠르게 파악하게 한다.
- 검색과 필터를 결합해 원하는 글을 찾기 쉽게 만든다.
- 관심 기업을 구독하면 새 글 알림을 받을 수 있게 한다.

## 핵심 기능

### 기술 블로그 아카이브

승인된 기업 기술 블로그의 RSS/Atom 피드를 수집하고, 중복을 제거한 뒤 공개 가능한 글을 아카이브한다. 원문 전문은 저장하거나 재배포하지 않고, 원문 링크와 출처를 명확히 제공한다.

### 다중 분류와 필터

하나의 글은 대표 기업 1개와 여러 직군 카테고리, 여러 주제 태그를 가질 수 있다. 사용자는 기업, 직군, 태그를 조합해 글 목록을 좁힐 수 있다.

### AI 요약

수집된 글은 AI 요약 파이프라인을 거쳐 핵심 요약과 주요 포인트를 제공한다. 요약은 원문을 대체하기 위한 기능이 아니라, 원문을 읽기 전에 판단 비용을 낮추기 위한 보조 정보다.

### 검색

MVP에서는 제목, 요약, 기업명, 직군, 태그를 대상으로 기본 검색을 제공한다. Elasticsearch 기반 고도화 검색은 후속 단계에서 도입한다.

### 구독과 알림

사용자는 계정 가입 없이 이메일 기반으로 관심 기업을 구독할 수 있다. 구독한 기업의 새 글이 게시되면 알림을 받을 수 있다.

### 관리자 운영

운영자는 소스 승인, 수집 상태 확인, 글 게시 상태 변경, 태그 보정, 요약 재생성 요청을 처리할 수 있다.

## MVP 범위

MVP는 2026년 5월 8일 배포를 목표로 한다.

포함 범위:

- 공개 최신 글 목록
- 글 상세와 AI 요약
- 기업, 직군, 태그 필터
- 기본 검색
- 기업별 글 목록
- 태그별 글 목록
- 이메일 기반 기업 구독
- RSS/Atom 기반 수집
- 최소 관리자 기능

제외 범위:

- 원문 전문 호스팅
- 일반 사용자 계정
- 댓글, 좋아요, 커뮤니티 기능
- 모바일 앱 푸시
- 개인화 추천 피드
- Elasticsearch 운영 적용

## 기술 방향

초기 구조는 프론트엔드와 백엔드를 하나의 repository에서 관리하는 모노레포를 기준으로 한다.

- Web: Next.js + TypeScript
- API: Spring Boot 3.5.x + Java 21
- Database: PostgreSQL
- Migration: Flyway
- Batch Worker: 초기에는 Spring Boot API 애플리케이션 안에 통합
- Search: MVP는 DB 기반 검색, 이후 Elasticsearch로 고도화

현재 repository에는 실제 구현을 시작하기 위한 root tooling과 Git hook 설정이 들어 있다. 이전 구현 초안은 검토 전까지 Git 추적 대상에서 제외된 `검토-필요/` 아래에 둔다.

## 문서

프로젝트 문서는 GitHub Wiki를 단일 문서 원천으로 사용한다. 이 repository에는 장기 문서를 두지 않는다.

- Wiki: https://github.com/TechLog-Hub/TlogHub/wiki
- 제품 요구사항: https://github.com/TechLog-Hub/TlogHub/wiki/requirements-product-overview
- MVP 착수 기준: https://github.com/TechLog-Hub/TlogHub/wiki/requirements-mvp-delivery-baseline
- 기능 요구사항: https://github.com/TechLog-Hub/TlogHub/wiki/requirements-functional
- 백엔드 구조와 언어 결정: https://github.com/TechLog-Hub/TlogHub/wiki/decision-backend-structure-language
- 문서 정책: https://github.com/TechLog-Hub/TlogHub/wiki/documentation-policy

## 개발 환경

Root tooling은 repository 검증과 Git hook 설정을 담당한다.

```bash
npm install
npm run validate:repo
```

API는 repository root의 Gradle wrapper로 실행한다.

```bash
./gradlew :apps:api:test
./gradlew :apps:api:bootRun
```

Windows PowerShell에서는 다음 명령을 사용한다.

```powershell
.\gradlew.bat :apps:api:test
.\gradlew.bat :apps:api:bootRun
```

기본 API 주소는 `http://localhost:8080`이며, Actuator health endpoint는 `http://localhost:8080/actuator/health`다.

Husky는 local Git hook을 설치한다. macOS와 Windows에서 동일하게 동작하도록 `.gitattributes`로 Husky hook과 Node.js 검증 스크립트의 LF line ending을 고정한다.

CI와 production install에서는 `HUSKY=0`을 사용한다.

커밋 메시지 제목은 gitmoji와 한국어 명사형 요약을 함께 사용한다.
제목 요약은 `~한다` 같은 서술형이 아니라 `추가`, `보강`, `수정`처럼 간결한 명사형으로 작성한다.

```text
✨ feat: 기업별 최신 글 목록 추가
📝 docs(readme): 서비스 소개 보강
🔧 chore(repo): 허스키 검증 규칙 수정
```

## 현재 상태

- Wiki에 요구사항, 화면 설계, 백엔드/API/Batch/Search 테크스펙, 스프린트 계획이 정리되어 있다.
- repository root에는 Husky 기반 검증과 문서 원천 정책이 설정되어 있다.
- 실제 앱 구현은 확정된 구조에 맞춰 새로 시작한다.
- 검토가 필요한 과거 구현 초안은 `검토-필요/` 아래에 격리되어 있으며 Git 추적 대상이 아니다.
