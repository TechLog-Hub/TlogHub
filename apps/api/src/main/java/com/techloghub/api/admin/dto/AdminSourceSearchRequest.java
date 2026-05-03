package com.techloghub.api.admin.dto;

import com.techloghub.api.common.error.BusinessException;
import com.techloghub.api.common.error.CommonErrorCode;
import com.techloghub.api.common.util.EnumParser;
import com.techloghub.api.common.util.StringNormalizer;
import com.techloghub.api.content.domain.SourceBlogStatus;

/**
 * 관리자 소스 목록 query parameter binding DTO다.
 */
public record AdminSourceSearchRequest(
	String q,
	String status,
	Integer page,
	Integer size
) {
	private static final int DEFAULT_PAGE = 0;
	private static final int DEFAULT_SIZE = 50;
	private static final int MAX_SIZE = 200;

	public String keyword() {
		return StringNormalizer.trimToNull(q);
	}

	public SourceBlogStatus parsedStatus() {
		return parseEnum(SourceBlogStatus.class, status, "status");
	}

	public int pageValue() {
		int value = page == null ? DEFAULT_PAGE : page;
		validatePage(value);
		return value;
	}

	public int sizeValue() {
		int value = size == null ? DEFAULT_SIZE : size;
		validateSize(value);
		return value;
	}

	private <E extends Enum<E>> E parseEnum(Class<E> enumType, String value, String fieldName) {
		E parsed = EnumParser.findNameIgnoreCase(enumType, value);
		if (StringNormalizer.trimToNull(value) != null && parsed == null) {
			throw new BusinessException(CommonErrorCode.INVALID_REQUEST, fieldName + " 값이 올바르지 않습니다.");
		}
		return parsed;
	}

	private void validatePage(int value) {
		if (value < DEFAULT_PAGE) {
			throw new BusinessException(CommonErrorCode.INVALID_REQUEST, "page는 0 이상이어야 합니다.");
		}
	}

	private void validateSize(int value) {
		if (value < 1 || value > MAX_SIZE) {
			throw new BusinessException(CommonErrorCode.INVALID_REQUEST, "size는 1 이상 " + MAX_SIZE + " 이하여야 합니다.");
		}
	}
}
