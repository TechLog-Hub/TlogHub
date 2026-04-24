package com.techloghub.api.curation.api;

import com.techloghub.api.curation.application.CuratedPostSummary;
import com.techloghub.api.curation.application.SampleCurationService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/curated-posts")
@CrossOrigin(origins = "http://localhost:3000")
public class CurationController {

    private final SampleCurationService sampleCurationService;

    public CurationController(SampleCurationService sampleCurationService) {
        this.sampleCurationService = sampleCurationService;
    }

    @GetMapping
    public List<CuratedPostSummary> findCuratedPosts() {
        return sampleCurationService.getCuratedPosts();
    }
}
