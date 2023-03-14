/*
 * Copyright 2022-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.jpa.repository.query;

import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Implementation of {@link QueryEnhancer} to enhance JPA queries using a {@link JpaQueryParserSupport}.
 *
 * @author Greg Turnquist
 * @author Mark Paluch
 * @since 3.1
 * @see JpqlQueryParser
 * @see HqlQueryParser
 */
class JpaQueryEnhancer implements QueryEnhancer {

	private final DeclaredQuery query;
	private final JpaQueryParserSupport queryParser;

	/**
	 * Initialize with an {@link JpaQueryParserSupport}.
	 *
	 * @param query
	 * @param queryParser
	 */
	private JpaQueryEnhancer(DeclaredQuery query, JpaQueryParserSupport queryParser) {

		this.query = query;
		this.queryParser = queryParser;
	}

	/**
	 * Factory method to create a {@link JpaQueryParserSupport} for {@link DeclaredQuery} using JPQL grammar.
	 *
	 * @param query must not be {@literal null}.
	 * @return a new {@link JpaQueryEnhancer} using JPQL.
	 */
	public static JpaQueryEnhancer forJpql(DeclaredQuery query) {

		Assert.notNull(query, "DeclaredQuery must not be null!");

		return new JpaQueryEnhancer(query, new JpqlQueryParser(query.getQueryString()));
	}

	/**
	 * Factory method to create a {@link JpaQueryParserSupport} for {@link DeclaredQuery} using HQL grammar.
	 *
	 * @param query must not be {@literal null}.
	 * @return a new {@link JpaQueryEnhancer} using HQL.
	 */
	public static JpaQueryEnhancer forHql(DeclaredQuery query) {

		Assert.notNull(query, "DeclaredQuery must not be null!");

		return new JpaQueryEnhancer(query, new HqlQueryParser(query.getQueryString()));
	}

	protected JpaQueryParserSupport getQueryParsingStrategy() {
		return queryParser;
	}

	/**
	 * Adds an {@literal order by} clause to the JPA query.
	 *
	 * @param sort the sort specification to apply.
	 * @return
	 */
	@Override
	public String applySorting(Sort sort) {
		return queryParser.renderSortedQuery(sort);
	}

	/**
	 * Because the parser can find the alias of the FROM clause, there is no need to "find it" in advance.
	 *
	 * @param sort the sort specification to apply.
	 * @param alias IGNORED
	 * @return
	 */
	@Override
	public String applySorting(Sort sort, String alias) {
		return applySorting(sort);
	}

	/**
	 * Resolves the alias for the entity in the FROM clause from the JPA query. Since the {@link JpaQueryParserSupport}
	 * can already find the alias when generating sorted and count queries, this is mainly to serve test cases.
	 */
	@Override
	public String detectAlias() {
		return queryParser.findAlias();
	}

	/**
	 * Creates a count query from the original query, with no count projection.
	 *
	 * @return Guaranteed to be not {@literal null};
	 */
	@Override
	public String createCountQueryFor() {
		return createCountQueryFor(null);
	}

	/**
	 * Create a count query from the original query, with potential custom projection.
	 *
	 * @param countProjection may be {@literal null}.
	 */
	@Override
	public String createCountQueryFor(@Nullable String countProjection) {
		return queryParser.createCountQuery(countProjection);
	}

	/**
	 * Checks if the select clause has a new constructor instantiation in the JPA query.
	 *
	 * @return Guaranteed to return {@literal true} or {@literal false}.
	 */
	@Override
	public boolean hasConstructorExpression() {
		return queryParser.hasConstructorExpression();
	}

	/**
	 * Looks up the projection of the JPA query. Since the {@link JpaQueryParserSupport} can already find the projection
	 * when generating sorted and count queries, this is mainly to serve test cases.
	 */
	@Override
	public String getProjection() {
		return queryParser.projection();
	}

	/**
	 * Since the {@link JpaQueryParserSupport} can already fully transform sorted and count queries by itself, this is a
	 * placeholder method.
	 *
	 * @return empty set
	 */
	@Override
	public Set<String> getJoinAliases() {
		return Set.of();
	}

	/**
	 * Look up the {@link DeclaredQuery} from the {@link JpaQueryParserSupport}.
	 */
	@Override
	public DeclaredQuery getQuery() {
		return query;
	}
}
