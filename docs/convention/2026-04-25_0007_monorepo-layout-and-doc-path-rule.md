# Monorepo Layout And Doc Path Rule

## Rule

- 웹 프론트엔드는 `apps/web/` 아래에 둔다.
- Spring 백엔드는 `apps/api/` 아래에 둔다.
- 프로젝트 계획은 `docs/plan/`에 둔다.
- 프로젝트 트러블슈팅은 `docs/troubleshooting/`에 둔다.
- 프론트엔드 전용 재사용 UI는 `apps/web/src/components/`와 `apps/web/src/data/`에서 관리한다.
- 백엔드 샘플 API는 `apps/api/src/main/java/com/techloghub/api/` 아래에서 계층별로 분리한다.

## Intent

초기 단계부터 앱 경계를 분리해 두면 Next.js와 Spring Boot의 빌드, 실행, 배포 흐름을 서로 섞지 않고 확장할 수 있다.
