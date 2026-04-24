package com.techloghub.api.curation.application;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SampleCurationService {

    public List<CuratedPostSummary> getCuratedPosts() {
        return List.of(
                new CuratedPostSummary(
                        "react-compiler-watch",
                        "React Compiler 도입 전에 렌더 규칙부터 정리하기",
                        "React Blog",
                        "Frontend",
                        "컴포넌트 순수성과 상태 소유권을 먼저 정리해야 한다는 흐름을 담은 샘플 데이터다.",
                        "8 min read",
                        "Today",
                        true
                ),
                new CuratedPostSummary(
                        "spring-runtime-signals",
                        "Spring 서비스의 운영 신호를 로그보다 메트릭에서 읽기",
                        "Spring",
                        "Backend",
                        "운영 품질과 관측 가능성을 우선으로 큐레이션하는 예시 항목이다.",
                        "9 min read",
                        "2 days ago",
                        true
                ),
                new CuratedPostSummary(
                        "cloudflare-edge-cache",
                        "Edge 캐시 전략을 제품 경험으로 연결하는 방법",
                        "Cloudflare",
                        "Cloud",
                        "플랫폼 글을 속도 자체보다 사용자 경험과 비용 관점으로 읽는 예시다.",
                        "5 min read",
                        "4 days ago",
                        false
                )
        );
    }
}
