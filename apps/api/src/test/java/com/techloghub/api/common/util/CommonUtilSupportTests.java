package com.techloghub.api.common.util;

import static com.techloghub.api.testsupport.TestTags.UNIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(UNIT)
class CommonUtilSupportTests {

	@Test
	void normalizesStrings() {
		assertThat(StringNormalizer.trimToNull("  ")).isNull();
		assertThat(StringNormalizer.trimToNull("  Spring  ")).isEqualTo("Spring");
		assertThat(StringNormalizer.normalizeWhitespaceToNull("  Spring\tBoot   운영  ")).isEqualTo("Spring Boot 운영");
		assertThat(StringNormalizer.truncate("abcdef", 3)).isEqualTo("abc");
	}

	@Test
	void convertsCollectionToStrictMapAndChunks() {
		List<Item> items = List.of(new Item("a", 1), new Item("b", 2));

		Map<String, Item> itemMap = CollectionSupport.toMapStrict(items, Item::key);
		Map<String, Integer> valueMap = CollectionSupport.toValueMapStrict(items, Item::key, Item::value);
		List<List<Item>> chunks = CollectionSupport.chunk(items, 1);

		assertThat(itemMap).containsOnlyKeys("a", "b");
		assertThat(valueMap).containsEntry("a", 1).containsEntry("b", 2);
		assertThat(chunks).hasSize(2);
		assertThatThrownBy(() -> CollectionSupport.toMapStrict(List.of(new Item("a", 1), new Item("a", 2)), Item::key))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("중복 key");
		assertThatThrownBy(() -> CollectionSupport.toValueMapStrict(
			List.of(new Item("a", 1), new Item("a", 2)),
			Item::key,
			Item::value
		))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("중복 key");
	}

	@Test
	void parsesEnumAndBuildsCacheKey() {
		assertThat(EnumParser.parseNameIgnoreCase(SampleState.class, "ready", "state")).isEqualTo(SampleState.READY);
		assertThat(EnumParser.allowedNames(SampleState.class)).isEqualTo("READY");
		assertThat(KeySupport.cacheKeyOf("Public Filter", "Toss", 1)).isEqualTo("public-filter:toss:1");
	}

	@Test
	void normalizesUrlSlugCryptoAndMasking() {
		assertThat(UrlSupport.normalizeUrl("HTTPS://Example.COM/docs///?q=spring"))
			.isEqualTo("https://example.com/docs?q=spring");
		assertThat(SlugSupport.slugify("Spring Boot 운영 경험")).isEqualTo("spring-boot");
		assertThatThrownBy(() -> SlugSupport.slugify("운영 경험"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("slug 생성 결과");
		assertThat(CryptoSupport.sha256Hex("techlog")).hasSize(64);
		assertThat(MaskingSupport.maskEmail("tester@example.com")).isEqualTo("te***@example.com");
		assertThat(MaskingSupport.maskSecret("1234567890abcdef")).isEqualTo("1234***cdef");
	}

	private record Item(String key, int value) {
	}

	private enum SampleState {
		READY
	}
}
