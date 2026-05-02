package com.techloghub.api.content.repository.impl;

import static com.techloghub.api.content.domain.QArchivedPost.archivedPost;
import static com.techloghub.api.content.domain.QCompany.company;
import static com.techloghub.api.content.domain.QSourceBlog.sourceBlog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.techloghub.api.content.domain.VisibilityState;
import com.techloghub.api.content.repository.ArchivedPostListQueryDto;
import com.techloghub.api.content.repository.ArchivedPostQueryRepository;
import com.techloghub.api.content.repository.ArchivedPostSearchCondition;

import lombok.RequiredArgsConstructor;

/**
 * 공개 글 목록의 동적 필터, projection, count 분리를 담당하는 QueryDSL 구현체다.
 */
@Repository
@RequiredArgsConstructor
public class ArchivedPostRepositoryImpl implements ArchivedPostQueryRepository {

	private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "title", "publishedAt");

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<ArchivedPostListQueryDto> searchPublished(ArchivedPostSearchCondition condition, Pageable pageable) {
		ArchivedPostSearchCondition safeCondition = condition == null ? ArchivedPostSearchCondition.all() : condition;
		BooleanExpression whereCondition = nullSafeBuilder(
			publishedOnly(),
			keywordContainsAllTokens(safeCondition.keyword()),
			companySlugEq(safeCondition.companySlug()),
			jobCategoryCodeEq(safeCondition.jobCategoryCode()),
			topicTagSlugEq(safeCondition.topicTagSlug())
		);

		List<ArchivedPostListQueryDto> content = queryFactory
			.select(Projections.constructor(
				ArchivedPostListQueryDto.class,
				archivedPost.id,
				archivedPost.slug,
				archivedPost.title,
				archivedPost.originUrl,
				company.slug,
				company.nameKo,
				sourceBlog.name,
				archivedPost.publishedAt
			))
			.from(archivedPost)
			.join(archivedPost.company, company)
			.join(archivedPost.sourceBlog, sourceBlog)
			.where(whereCondition)
			.orderBy(orderSpecifiers(pageable))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = queryFactory
			.select(archivedPost.id.count())
			.from(archivedPost)
			.where(whereCondition);

		return PageableExecutionUtils.getPage(
			content,
			pageable,
			() -> Objects.requireNonNullElse(countQuery.fetchOne(), 0L)
		);
	}

	private OrderSpecifier<?>[] orderSpecifiers(Pageable pageable) {
		List<OrderSpecifier<?>> orders = new ArrayList<>();
		boolean idSorted = false;
		for (Sort.Order order : pageable.getSort()) {
			toOrderSpecifier(order).ifPresent(orders::add);
			idSorted = idSorted || "id".equals(order.getProperty());
		}
		if (orders.isEmpty()) {
			orders.add(archivedPost.publishedAt.desc());
		}
		if (!idSorted) {
			orders.add(archivedPost.id.desc());
		}
		return orders.toArray(OrderSpecifier[]::new);
	}

	private Optional<OrderSpecifier<?>> toOrderSpecifier(Sort.Order order) {
		com.querydsl.core.types.Order direction = order.isAscending()
			? com.querydsl.core.types.Order.ASC
			: com.querydsl.core.types.Order.DESC;
		if (!ALLOWED_SORT_FIELDS.contains(order.getProperty())) {
			return Optional.empty();
		}
		return switch (order.getProperty()) {
			case "id" -> Optional.of(new OrderSpecifier<>(direction, archivedPost.id));
			case "title" -> Optional.of(new OrderSpecifier<>(direction, archivedPost.title));
			case "publishedAt" -> Optional.of(new OrderSpecifier<>(direction, archivedPost.publishedAt));
			default -> Optional.empty();
		};
	}

	private BooleanExpression publishedOnly() {
		return archivedPost.visibilityState.eq(VisibilityState.PUBLISHED);
	}

	private BooleanExpression keywordContainsAllTokens(String keyword) {
		String normalizedKeyword = emptyToNull(keyword);
		if (normalizedKeyword == null) {
			return null;
		}

		BooleanExpression expression = null;
		for (String token : normalizedKeyword.split("\\s+")) {
			BooleanExpression tokenExpression = archivedPost.title.containsIgnoreCase(token);
			expression = expression == null ? tokenExpression : expression.and(tokenExpression);
		}
		return expression;
	}

	private BooleanExpression companySlugEq(String companySlug) {
		String normalizedCompanySlug = emptyToNull(companySlug);
		return normalizedCompanySlug == null ? null : archivedPost.company.slug.eq(normalizedCompanySlug);
	}

	private BooleanExpression jobCategoryCodeEq(String jobCategoryCode) {
		String normalizedJobCategoryCode = emptyToNull(jobCategoryCode);
		return normalizedJobCategoryCode == null
			? null
			: archivedPost.jobCategories.any().code.eq(normalizedJobCategoryCode);
	}

	private BooleanExpression topicTagSlugEq(String topicTagSlug) {
		String normalizedTopicTagSlug = emptyToNull(topicTagSlug);
		return normalizedTopicTagSlug == null ? null : archivedPost.topicTags.any().slug.eq(normalizedTopicTagSlug);
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

	private String emptyToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
