# T-Log Copilot Review Instructions

리뷰는 한국어로 작성한다.

## Project Context

- T-Log는 기업 기술 블로그 글을 수집하고 AI 요약, 기업/직군/태그 필터, 구독 알림을 제공하는 서비스다.
- GitHub 기본 브랜치는 `develop`이다.
- `main`은 production 기준 브랜치다.
- 일반 기능 PR은 `develop`을 base로 둔다.
- 프로젝트 문서 원천은 GitHub Wiki다. main repo에는 장기 문서용 `docs/`를 두지 않는다.

## Review Priorities

- 요구사항과 Wiki 설계에서 벗어난 구현이 있는지 확인한다.
- branch, PR, hook, commit workflow를 우회하는 변경이 있는지 확인한다.
- Spring 백엔드는 Java 21, Spring Boot 3.5.x, Maven wrapper 기준을 따른다.
- Spring 설정 파일은 `application.yml`과 profile별 `application-<profile>.yml`만 사용한다.
- JPA entity는 public setter를 남발하지 않고 factory와 business method 중심으로 상태를 변경해야 한다.
- Flyway migration과 JPA mapping이 서로 다른 제약을 표현하지 않는지 확인한다.
- 보안상 token, webhook URL, credential, personal data가 commit에 포함되지 않는지 확인한다.

## Comment Style

- blocker, risk, suggestion을 구분해서 작성한다.
- 단순 취향보다 요구사항, 유지보수성, 테스트 가능성, 운영 리스크를 우선한다.
- Copilot review는 approval을 대체하지 않는다. 사람 리뷰어가 최종 판단한다.
