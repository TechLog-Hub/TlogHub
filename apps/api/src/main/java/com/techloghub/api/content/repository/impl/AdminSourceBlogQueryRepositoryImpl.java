package com.techloghub.api.content.repository.impl;

import static com.techloghub.api.content.domain.QCompany.company;
import static com.techloghub.api.content.domain.QSourceBlog.sourceBlog;

import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.techloghub.api.content.repository.AdminSourceBlogQueryDto;
import com.techloghub.api.content.repository.AdminSourceBlogQueryRepository;
import com.techloghub.api.content.repository.AdminSourceBlogSearchCondition;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 소스 목록의 동적 필터와 projection 조회를 담당한다.
 */
@RequiredArgsConstructor
public class AdminSourceBlogQueryRepositoryImpl implements AdminSourceBlogQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<AdminSourceBlogQueryDto> searchAdminSources(
		AdminSourceBlogSearchCondition condition,
		Pageable pageable
	) {
		AdminSourceBlogSearchCondition safeCondition = condition == null
			? AdminSourceBlogSearchCondition.of(null, null)
			: condition;
		BooleanExpression whereCondition = nullSafeBuilder(
			statusEq(safeCondition),
			keywordContains(safeCondition.keyword())
		);

		var content = queryFactory
			.select(Projections.constructor(
				AdminSourceBlogQueryDto.class,
				sourceBlog.id,
				company.slug,
				company.nameKo,
				sourceBlog.name,
				sourceBlog.homepageUrl,
				sourceBlog.feedUrl,
				sourceBlog.sourceType,
				sourceBlog.status,
				sourceBlog.reviewReason,
				sourceBlog.lastCollectedAt,
				sourceBlog.createdAt
			))
			.from(sourceBlog)
			.join(sourceBlog.company, company)
			.where(whereCondition)
			.orderBy(sourceBlog.id.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = queryFactory
			.select(sourceBlog.id.count())
			.from(sourceBlog)
			.join(sourceBlog.company, company)
			.where(whereCondition);

		return PageableExecutionUtils.getPage(
			content,
			pageable,
			() -> Objects.requireNonNullElse(countQuery.fetchOne(), 0L)
		);
	}

	private BooleanExpression statusEq(AdminSourceBlogSearchCondition condition) {
		return condition.status() == null ? null : sourceBlog.status.eq(condition.status());
	}

	private BooleanExpression keywordContains(String keyword) {
		if (keyword == null) {
			return null;
		}
		return sourceBlog.name.containsIgnoreCase(keyword)
			.or(sourceBlog.homepageUrl.containsIgnoreCase(keyword))
			.or(sourceBlog.feedUrl.containsIgnoreCase(keyword))
			.or(company.nameKo.containsIgnoreCase(keyword))
			.or(company.nameEn.containsIgnoreCase(keyword));
	}

	private BooleanExpression nullSafeBuilder(BooleanExpression... expressions) {
		BooleanExpression result = null;
		for (BooleanExpression expression : expressions) {
			if (expression != null) {
				result = result == null ? expression : result.and(expression);
			}
		}
		return result;
	}
}
