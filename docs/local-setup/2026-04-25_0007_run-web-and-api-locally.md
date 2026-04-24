# Run Web And Api Locally

## Prerequisites

- Node.js 22 이상
- npm 10 이상
- Java 21

## Web

```bash
cd apps/web
npm install
npm run dev
```

기본적으로 Next.js 개발 서버는 `http://localhost:3000`에서 실행된다.

## Api

```powershell
cd apps/api
.\mvnw.cmd spring-boot:run
```

기본적으로 Spring Boot API는 `http://localhost:8080`에서 실행된다.

## Validation

```bash
cd apps/web
npm run lint
npm run build
```

```powershell
cd apps/api
.\mvnw.cmd test
```
