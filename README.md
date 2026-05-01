# Techlog Hub

기술 블로그를 한 화면에서 모아 보고, 읽을 만한 글만 빠르게 추리는 서비스의 초기 모노레포 골격이다.

## 구조

```text
techlog-hub/
```

문서는 GitHub Wiki에서 관리한다.

- Wiki: https://github.com/TechLog-Hub/TlogHub/wiki
- 문서 정책: https://github.com/TechLog-Hub/TlogHub/wiki/documentation-policy

구현 초안은 검토 전까지 Git 추적 대상에서 제외한다.

- 검토 폴더: `검토-필요/`
- 현재 앱 초안: `검토-필요/apps/`

## 현재 상태

- `검토-필요/apps/web`
  검색과 토픽 필터가 가능한 Next.js 데모 큐레이션 화면
- `검토-필요/apps/api`
  Flyway/JPA 기반 공개 조회, 구독, 관리자, RSS/AI Batch Worker, 검색 추상화가 포함된 Spring Boot API
- Wiki
  요구사항, 화면 설계, 백엔드/API/Batch/Search 테크스팩, 계획 문서의 단일 문서 원천

## 실행

### Web

```bash
cd 검토-필요/apps/web
npm install
npm run dev
```

### API

```powershell
cd 검토-필요/apps/api
.\mvnw.cmd spring-boot:run
```

## 검증

### Web

```bash
cd 검토-필요/apps/web
npm run lint
npm run build
```

### API

```powershell
cd 검토-필요/apps/api
.\mvnw.cmd test
```

## 다음 단계

1. API 서버 hardening 테스트와 운영 보안 정책을 보강한다.
2. Batch Worker의 동시 실행 방지, 재시작, 수집 fixture 테스트를 구현한다.
3. Elasticsearch provider parity, reindex, alias 전환 전략을 구현한다.
