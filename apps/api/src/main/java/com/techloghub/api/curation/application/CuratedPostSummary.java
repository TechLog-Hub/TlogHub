package com.techloghub.api.curation.application;

public record CuratedPostSummary(
        String id,
        String title,
        String source,
        String topic,
        String summary,
        String readingTime,
        String freshness,
        boolean highlight
) {
}
