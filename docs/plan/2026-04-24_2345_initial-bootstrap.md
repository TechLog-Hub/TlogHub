# Plan: initial bootstrap

**Project**: Techlog Hub  
**Status**: Done  
**Created**: 2026-04-24 23:45  
**Last Updated**: 2026-04-25 00:07  
**Related Scope**: project  
**Related Context**: 기술 블로그 큐레이션 사이트 초기 부트스트랩  
**Related Commit**:

## Goal

기술 블로그를 한곳에 모아 보여주는 사이트의 첫 버전을 빠르게 시작할 수 있는 상태를 만든다.

## Success Criteria

- [x] 외부 프로젝트 저장소와 registry entry가 생성된다.
- [x] `apps/web` Next.js 프론트와 `apps/api` Spring Boot 백엔드 골격이 준비된다.
- [x] `docs/`, `docs/plan/`, `docs/troubleshooting/` 구조와 초기 문서가 준비된다.

## Context

- Relevant docs: `docs/architecture/`, `docs/local-setup/`, `docs/stack-selection/`, `docs/api/`
- Constraints: 빠른 시작이 우선이며 아직 실제 수집 백엔드와 저장소는 없다.
- Out of scope: 사용자 인증, RSS 파이프라인, 데이터베이스, 개인화 추천

## Phase Plan

### Phase 1: project bootstrap

**Goal**: 저장소와 기본 문서 구조를 준비한다.

**Tasks**

- [x] 외부 저장소 생성
- [x] 하네스 registry 등록
- [x] 프로젝트 문서 골격 부트스트랩

**Validation**

- [x] `project/registry.yaml`에 프로젝트가 등록된다.
- [x] `docs/index.md`, `plan/index.md`, `troubleshooting/index.md`가 존재한다.

### Phase 2: monorepo scaffold

**Goal**: 웹과 API를 분리한 초기 모노레포 구조를 만든다.

**Tasks**

- [x] `apps/web` Next.js + TypeScript 프로젝트 생성
- [x] `apps/api` Spring Boot 프로젝트 생성
- [x] 데모 데이터와 검색/필터 UI, 샘플 API 구현
- [x] README와 초기 프로젝트 문서 작성

**Validation**

- [ ] `cd apps/web && npm run lint`
- [ ] `cd apps/web && npm run build`
- [ ] `cd apps/api && .\mvnw.cmd test`

## Risks / Blockers

- 실제 수집 파이프라인이 없어서 현재 콘텐츠는 정적 예시 데이터와 샘플 API다.

## Decisions / Trade-offs

- 웹과 API의 실행 경계가 다르므로 `apps/web`, `apps/api` 모노레포 구조를 먼저 잡는다.
- 문서 추적을 단순화하기 위해 계획과 트러블슈팅도 `docs/` 아래에서 함께 관리한다.

## Rollback / Recovery

- 프로젝트를 중단하려면 외부 저장소를 제거하고 하네스 registry entry를 삭제한다.

## Next Action

RSS 수집기 후보와 저장 방식을 정한 뒤 `apps/api` 실제 데이터 파이프라인 설계를 시작한다.
