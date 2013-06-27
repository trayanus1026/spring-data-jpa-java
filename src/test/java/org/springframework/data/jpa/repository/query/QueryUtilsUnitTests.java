/*
 * Copyright 2008-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.jpa.repository.query;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.data.jpa.repository.query.QueryUtils.*;

import java.util.Set;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.springframework.data.domain.Sort;

/**
 * Unit test for {@link QueryUtils}.
 * 
 * @author Oliver Gierke
 */
public class QueryUtilsUnitTests {

	static final String QUERY = "select u from User u";
	static final String FQ_QUERY = "select u from org.acme.domain.User$Foo_Bar u";
	static final String SIMPLE_QUERY = "from User u";
	static final String COUNT_QUERY = "select count(u) from User u";

	static final String QUERY_WITH_AS = "select u from User as u where u.username = ?";

	static final Matcher<String> IS_U = is("u");

	@Test
	public void createsCountQueryCorrectly() throws Exception {

		assertCountQuery(QUERY, COUNT_QUERY);

	}

	/**
	 * @see #303
	 */
	@Test
	public void createsCountQueriesCorrectlyForCapitalLetterJPQL() {

		assertCountQuery("FROM User u WHERE u.foo.bar = ?", "select count(u) FROM User u WHERE u.foo.bar = ?");

		assertCountQuery("SELECT u FROM User u where u.foo.bar = ?", "select count(u) FROM User u where u.foo.bar = ?");
	}

	/**
	 * @see #351
	 */
	@Test
	public void createsCountQueryForDistinctQueries() throws Exception {

		assertCountQuery("select distinct u from User u where u.foo = ?",
				"select count(distinct u) from User u where u.foo = ?");
	}

	/**
	 * @see #351
	 */
	@Test
	public void createsCountQueryForConstructorQueries() throws Exception {

		assertCountQuery("select distinct new User(u.name) from User u where u.foo = ?",
				"select count(distinct u) from User u where u.foo = ?");
	}

	/**
	 * @see #352
	 */
	@Test
	public void createsCountQueryForJoins() throws Exception {

		assertCountQuery("select distinct new User(u.name) from User u left outer join u.roles r WHERE r = ?",
				"select count(distinct u) from User u left outer join u.roles r WHERE r = ?");
	}

	/**
	 * @see #352
	 */
	@Test
	public void createsCountQueryForQueriesWithSubSelects() throws Exception {

		assertCountQuery("select u from User u left outer join u.roles r where r in (select r from Role)",
				"select count(u) from User u left outer join u.roles r where r in (select r from Role)");
	}

	/**
	 * @see #355
	 */
	@Test
	public void createsCountQueryForAliasesCorrectly() throws Exception {

		assertCountQuery("select u from User as u", "select count(u) from User as u");
	}

	@Test
	public void allowsShortJpaSyntax() throws Exception {

		assertCountQuery(SIMPLE_QUERY, COUNT_QUERY);
	}

	@Test
	public void detectsAliasCorrectly() throws Exception {

		assertThat(detectAlias(QUERY), IS_U);
		assertThat(detectAlias(SIMPLE_QUERY), IS_U);
		assertThat(detectAlias(COUNT_QUERY), IS_U);
		assertThat(detectAlias(QUERY_WITH_AS), IS_U);
		assertThat(detectAlias("SELECT FROM USER U"), is("U"));
		assertThat(detectAlias("select u from  User u"), IS_U);
		assertThat(detectAlias("select u from  com.acme.User u"), IS_U);
		assertThat(detectAlias("select u from T05User u"), IS_U);
	}

	@Test
	public void allowsFullyQualifiedEntityNamesInQuery() {

		assertThat(detectAlias(FQ_QUERY), IS_U);
		assertCountQuery(FQ_QUERY, "select count(u) from org.acme.domain.User$Foo_Bar u");
	}

	/**
	 * @see DATAJPA-252
	 */
	@Test
	public void detectsJoinAliasesCorrectly() {

		Set<String> aliases = getOuterJoinAliases("select p from Person p left outer join x.foo b2_$ar where …");
		assertThat(aliases, hasSize(1));
		assertThat(aliases, hasItems("b2_$ar"));

		aliases = getOuterJoinAliases("select p from Person p left join x.foo b2_$ar where …");
		assertThat(aliases, hasSize(1));
		assertThat(aliases, hasItems("b2_$ar"));

		aliases = getOuterJoinAliases("select p from Person p left outer join x.foo as b2_$ar, left join x.bar as foo where …");
		assertThat(aliases, hasSize(2));
		assertThat(aliases, hasItems("b2_$ar", "foo"));

		aliases = getOuterJoinAliases("select p from Person p left join x.foo as b2_$ar, left outer join x.bar foo where …");
		assertThat(aliases, hasSize(2));
		assertThat(aliases, hasItems("b2_$ar", "foo"));
	}

	/**
	 * @see DATAJPA-252
	 */
	@Test
	public void doesNotPrefixOrderReferenceIfOuterJoinAliasDetected() {

		String query = "select p from Person p left join p.address address";
		assertThat(applySorting(query, new Sort("address.city")), endsWith("order by address.city asc"));
		assertThat(applySorting(query, new Sort("address.city", "lastname"), "p"),
				endsWith("order by address.city asc, p.lastname asc"));
	}

	/**
	 * @see DATAJPA-252
	 */
	@Test
	public void extendsExistingOrderByClausesCorrectly() {

		String query = "select p from Person p order by p.lastname asc";
		assertThat(applySorting(query, new Sort("firstname"), "p"), endsWith("order by p.lastname asc, p.firstname asc"));
	}

	/**
	 * @see DATAJPA-342
	 */
	@Test
	public void usesReturnedVariableInCOuntProjectionIfSet() {

		assertCountQuery("select distinct m.genre from Media m where m.user = ?1 order by m.genre asc",
				"select count(distinct m.genre) from Media m where m.user = ?1 order by m.genre asc");
	}

	/**
	 * @see DATAJPA-343
	 */
	@Test
	public void projectsCOuntQueriesForQueriesWithSubselects() {

		assertCountQuery("select o from Foo o where cb.id in (select b from Bar b)",
				"select count(o) from Foo o where cb.id in (select b from Bar b)");
	}

	/**
	 * @see DATAJPA-148
	 */
	@Test
	public void doesNotPrefixSortsIfFunction() {

		Sort sort = new Sort("sum(foo)");
		assertThat(applySorting("select p from Person p", sort, "p"), endsWith("order by sum(foo) asc"));
	}

	private void assertCountQuery(String originalQuery, String countQuery) {
		assertThat(createCountQueryFor(originalQuery), is(countQuery));
	}
}
