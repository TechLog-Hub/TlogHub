# T-Log Gemini Code Assist Style Guide

Gemini Code Assist는 PR에서 높은 신뢰도의 문제만 짧게 남긴다.

## Review Language

- 모든 리뷰 코멘트는 한국어로 작성한다.
- 문제, 영향, 수정 방향을 한 문단 안에서 명확히 설명한다.
- 단순 취향이나 사소한 문장 교정은 남기지 않는다.

## Project Context

- T-Log는 기업 기술 블로그 글을 수집하고 AI 요약, 기업/직군/태그 필터, 구독 알림을 제공하는 서비스다.
- GitHub 기본 브랜치는 `develop`이다.
- `main`은 production 기준 브랜치다.
- 일반 기능 PR은 `develop`을 base로 둔다.
- 프로젝트 문서 원천은 GitHub Wiki다. main repo에는 장기 문서용 `docs/`를 두지 않는다.

## Review Priorities

- Wiki 요구사항과 설계에서 벗어난 동작 변경을 우선 확인한다.
- branch, PR, hook, commit workflow를 우회하는 변경을 확인한다.
- Spring 백엔드는 Java 21, Spring Boot 3.5.x, Maven wrapper 기준을 따른다.
- Spring 설정 파일은 `application.yml`과 profile별 `application-<profile>.yml`만 사용한다.
- JPA entity는 public setter를 남발하지 않고 factory와 business method 중심으로 상태를 변경한다.
- Flyway migration과 JPA mapping이 서로 다른 제약을 표현하지 않게 한다.
- token, webhook URL, credential, personal data가 commit에 포함되지 않게 한다.

## Noise Control

- blocker, production risk, data loss, security, test gap, workflow violation에 집중한다.
- 코드 스타일만 다른 경우에는 기존 프로젝트 컨벤션 위반이 명확할 때만 코멘트한다.
- 이미 PR 본문이나 커밋 메시지에 설명된 의도는 반복 요약하지 않는다.
