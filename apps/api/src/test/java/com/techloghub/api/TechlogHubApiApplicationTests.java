package com.techloghub.api;

import static com.techloghub.api.testsupport.TestTags.INTEGRATION;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@Tag(INTEGRATION)
class TechlogHubApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
