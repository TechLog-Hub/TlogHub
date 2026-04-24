# Sample Curation Endpoint

## Endpoint

- Method: `GET`
- Path: `/api/v1/curated-posts`

## Purpose

프론트 연동 전까지 사용할 샘플 큐레이션 데이터를 반환한다.

## Response Fields

- `id`
- `title`
- `source`
- `topic`
- `summary`
- `readingTime`
- `freshness`
- `highlight`

## Current Notes

- 현재는 메모리 기반 샘플 데이터만 반환한다.
- 실제 RSS 수집과 저장 계층이 붙으면 이 API가 실제 데이터 소스로 대체된다.
