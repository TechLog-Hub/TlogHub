# Next.js And Spring Choice

## Choice

프론트엔드는 `Next.js + TypeScript`, 백엔드는 `Spring Boot`로 시작한다.

## Why This Choice

- 기술 블로그 모음집은 검색 노출과 메타데이터 관리가 중요해서 Next.js App Router가 잘 맞는다.
- 백엔드는 RSS 수집, 저장, 개인화 추천, 스케줄링으로 확장될 가능성이 높아서 Spring Boot가 적합하다.
- 두 런타임의 빌드와 실행 방식이 다르므로 `apps/web`, `apps/api`로 경계를 분리하는 편이 유지보수에 유리하다.

## Deferred Decisions

- 데이터베이스와 ORM 선택은 실제 수집 파이프라인 요구가 정해진 뒤 결정한다.
- 인증과 사용자 개인화는 MVP 검증 뒤에 붙인다.
- 프론트가 Spring API를 직접 조회할지 BFF를 둘지는 실제 데이터 흐름이 생긴 뒤 재검토한다.
