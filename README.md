# Techlog Hub

기술 블로그를 한 화면에서 모아 보고, 읽을 만한 글만 빠르게 추리는 서비스의 초기 모노레포 골격이다.

## 구조

```text
techlog-hub/
  apps/
    web/   # Next.js + TypeScript
    api/   # Spring Boot
  docs/
    plan/
    troubleshooting/
```

## 현재 상태

- `apps/web`
  검색과 토픽 필터가 가능한 Next.js 데모 큐레이션 화면
- `apps/api`
  샘플 큐레이션 데이터를 반환하는 Spring Boot API 골격
- `docs/`
  설계, 스택 선택, 로컬 실행, 계획 문서

## 실행

### Web

```bash
cd apps/web
npm install
npm run dev
```

### API

```powershell
cd apps/api
.\mvnw.cmd spring-boot:run
```

## 검증

### Web

```bash
cd apps/web
npm run lint
npm run build
```

### API

```powershell
cd apps/api
.\mvnw.cmd test
```

## 다음 단계

1. RSS 수집기와 저장소를 붙여 정적 데이터를 실제 수집 데이터로 교체한다.
2. `apps/web`가 `apps/api`를 조회하도록 연동한다.
3. 카테고리, 난이도, 팀 관심도 기반 개인화 큐레이션을 설계한다.
