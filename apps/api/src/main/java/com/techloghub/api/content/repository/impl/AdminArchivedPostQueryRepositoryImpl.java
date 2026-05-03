package com.techloghub.api.content.repository.impl;

import static com.techloghub.api.content.domain.QAiSummary.aiSummary;
import static com.techloghub.api.content.domain.QArchivedPost.archivedPost;
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
import com.techloghub.api.content.repository.AdminArchivedPostQueryDto;
import com.techloghub.api.content.repository.AdminArchivedPostQueryRepository;
import com.techloghub.api.content.repository.AdminArchivedPostSearchCondition;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 글 목록의 동적 필터와 projection 조회를 담당한다.
 */
@RequiredArgsConstructor
public class AdminArchivedPostQueryRepositoryImpl implements AdminArchivedPostQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<AdminArchivedPostQueryDto> searchAdminPosts(
		AdminArchivedPostSearchCondition condition,
		Pageable pageable
	) {
		AdminArchivedPostSearchCondition safeCondition = condition == null
			? AdminArchivedPostSearchCondition.of(null, null, null, null, null)
			: condition;
		BooleanExpression whereCondition = nullSafeBuilder(
			keywordContains(safeCondition.keyword()),
			companySlugEq(safeCondition.companySlug()),
			processingStateEq(safeCondition),
			visibilityStateEq(safeCondition),
			summaryStateEq(safeCondition)
		);

		var content = queryFactory
			.select(Projections.constructor(
				AdminArchivedPostQueryDto.class,
				archivedPost.id,
				archivedPost.slug,
				archivedPost.title,
				archivedPost.originUrl,
				company.slug,
				company.nameKo,
				sourceBlog.name,
				archivedPost.publishedAt,
				archivedPost.collectedAt,
				archivedPost.processingState,
				archivedPost.visibilityState,
				archivedPost.reviewReason,
				aiSummary.summaryState
			))
			.from(archivedPost)
			.join(archivedPost.company, company)
			.join(archivedPost.sourceBlog, sourceBlog)
			.leftJoin(aiSummary)
			.on(aiSummary.archivedPost.eq(archivedPost).and(aiSummary.current.isTrue()))
			.where(whereCondition)
			.orderBy(archivedPost.publishedAt.desc(), archivedPost.id.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = queryFactory
			.select(archivedPost.id.count())
			.from(archivedPost)
			.join(archivedPost.company, company)
			.join(archivedPost.sourceBlog, sourceBlog)
			.leftJoin(aiSummary)
			.on(aiSummary.archivedPost.eq(archivedPost).and(aiSummary.current.isTrue()))
			.where(whereCondition);

		return PageableExecutionUtils.getPage(
			content,
			pageable,
			() -> Objects.requireNonNullElse(countQuery.fetchOne(), 0L)
		);
	}

	private BooleanExpression keywordContains(String keyword) {
		if (keyword == null) {
			return null;
		}
		return archivedPost.title.containsIgnoreCase(keyword)
			.or(archivedPost.originUrl.containsIgnoreCase(keyword));
	}

	private BooleanExpression companySlugEq(String companySlug) {
		return companySlug == null ? null : company.slug.eq(companySlug);
	}

	private BooleanExpression processingStateEq(AdminArchivedPostSearchCondition condition) {
		return condition.processingState() == null ? null : archivedPost.processingState.eq(condition.processingState());
	}

	private BooleanExpression visibilityStateEq(AdminArchivedPostSearchCondition condition) {
		return condition.visibilityState() == null ? null : archivedPost.visibilityState.eq(condition.visibilityState());
	}

	private BooleanExpression summaryStateEq(AdminArchivedPostSearchCondition condition) {
		return condition.summaryState() == null ? null : aiSummary.summaryState.eq(condition.summaryState());
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
