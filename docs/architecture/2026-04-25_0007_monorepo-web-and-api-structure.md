# Monorepo Web And Api Structure

## Summary

Techlog Hub는 단일 저장소 안에 `apps/web`과 `apps/api`를 둔 모노레포 구조로 시작한다.
프론트는 Next.js App Router 기반이고, 백엔드는 Spring Boot API 애플리케이션이다.

## Repository Layout

- `apps/web`
  Next.js + TypeScript 프론트엔드다. 현재는 정적 데모 데이터를 사용해 큐레이션 목록을 렌더링한다.
- `apps/api`
  Spring Boot 백엔드다. 현재는 샘플 큐레이션 JSON을 반환하는 REST API 골격을 가진다.
- `docs/`
  프로젝트 전용 설계, 스택 선택, 로컬 실행, 계획, 트러블슈팅 문서를 둔다.

## Runtime Shape

- `apps/web/src/app/page.tsx`
  홈 라우트의 엔트리다.
- `apps/web/src/components/curation-explorer.tsx`
  검색과 토픽 필터가 있는 인터랙티브 UI 경계다.
- `apps/api/src/main/java/com/techloghub/api/curation/api/CurationController.java`
  샘플 큐레이션 API를 노출한다.

## Integration Direction

1. 현재 웹은 정적 데이터를 사용한다.
2. 다음 단계에서 웹이 Spring API를 조회하도록 교체한다.
3. 이후 RSS 수집기, 저장 계층, 개인화 로직을 백엔드 쪽으로 확장한다.
