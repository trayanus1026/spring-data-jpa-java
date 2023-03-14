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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Implementation of {@link QueryEnhancer} using a {@link JpaQueryParser}.<br/>
 * <br/>
 * NOTE: The parser can find everything it needs to created sorted and count queries. Thus, looking up the alias or the
 * projection isn't needed for its primary function, and are simply implemented for test purposes.
 *
 * @author Greg Turnquist
 * @since 3.1
 */
class JpaQueryParsingEnhancer implements QueryEnhancer {

	private final JpaQueryParser queryParser;

	/**
	 * Initialize with an {@link JpaQueryParser}.
	 * 
	 * @param queryParser
	 */
	public JpaQueryParsingEnhancer(JpaQueryParser queryParser) {

		Assert.notNull(queryParser, "queryParse must not be null!");
		this.queryParser = queryParser;
	}

	public JpaQueryParser getQueryParsingStrategy() {
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
		return queryParser.createQuery(sort);
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
	 * Resolves the alias for the entity in the FROM clause from the JPA query. Since the {@link JpaQueryParser} can
	 * already find the alias when generating sorted and count queries, this is mainly to serve test cases.
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
	 * Looks up the projection of the JPA query. Since the {@link JpaQueryParser} can already find the projection when
	 * generating sorted and count queries, this is mainly to serve test cases.
	 */
	@Override
	public String getProjection() {
		return queryParser.projection();
	}

	/**
	 * Since the {@link JpaQueryParser} can already fully transform sorted and count queries by itself, this is a
	 * placeholder method.
	 *
	 * @return empty set
	 */
	@Override
	public Set<String> getJoinAliases() {
		return Set.of();
	}

	/**
	 * Look up the {@link DeclaredQuery} from the {@link JpaQueryParser}.
	 */
	@Override
	public DeclaredQuery getQuery() {
		return queryParser.getDeclaredQuery();
	}
}
