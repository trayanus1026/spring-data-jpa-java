/*
 * Copyright 2013-2015 the original author or authors.
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

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.data.jpa.repository.query.StringQuery.InParameterBinding;
import org.springframework.data.jpa.repository.query.StringQuery.LikeParameterBinding;
import org.springframework.data.jpa.repository.query.StringQuery.ParameterBinding;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.parser.Part.Type;

/**
 * Unit tests for {@link StringQuery}.
 * 
 * @author Oliver Gierke
 * @author Thomas Darimont
 */
public class StringQueryUnitTests {

	public @Rule ExpectedException exception = ExpectedException.none();

	/**
	 * @see DATAJPA-341
	 */
	@Test
	public void doesNotConsiderPlainLikeABinding() {

		String source = "select from User u where u.firstname like :firstname";
		StringQuery query = new StringQuery(source);

		assertThat(query.hasParameterBindings(), is(true));
		assertThat(query.getQueryString(), is(source));

		List<ParameterBinding> bindings = query.getParameterBindings();
		assertThat(bindings, hasSize(1));

		LikeParameterBinding binding = (LikeParameterBinding) bindings.get(0);
		assertThat(binding.getType(), is(Type.LIKE));
		assertThat(binding.hasName("firstname"), is(true));
	}

	@Test
	public void detectsPositionalLikeBindings() {

		StringQuery query = new StringQuery("select u from User u where u.firstname like %?1% or u.lastname like %?2");

		assertThat(query.hasParameterBindings(), is(true));
		assertThat(query.getQueryString(), is("select u from User u where u.firstname like ?1 or u.lastname like ?2"));

		List<ParameterBinding> bindings = query.getParameterBindings();
		assertThat(bindings, hasSize(2));

		LikeParameterBinding binding = (LikeParameterBinding) bindings.get(0);
		assertThat(binding, is(notNullValue()));
		assertThat(binding.hasPosition(1), is(true));
		assertThat(binding.getType(), is(Type.CONTAINING));

		binding = (LikeParameterBinding) bindings.get(1);
		assertThat(binding, is(notNullValue()));
		assertThat(binding.hasPosition(2), is(true));
		assertThat(binding.getType(), is(Type.ENDING_WITH));
	}

	@Test
	public void detectsNamedLikeBindings() {

		StringQuery query = new StringQuery("select u from User u where u.firstname like %:firstname");

		assertThat(query.hasParameterBindings(), is(true));
		assertThat(query.getQueryString(), is("select u from User u where u.firstname like :firstname"));

		List<ParameterBinding> bindings = query.getParameterBindings();
		assertThat(bindings, hasSize(1));

		LikeParameterBinding binding = (LikeParameterBinding) bindings.get(0);
		assertThat(binding, is(notNullValue()));
		assertThat(binding.hasName("firstname"), is(true));
		assertThat(binding.getType(), is(Type.ENDING_WITH));
	}

	/**
	 * @see DATAJPA-461
	 */
	@Test
	public void detectsNamedInParameterBindings() {

		String queryString = "select u from User u where u.id in :ids";
		StringQuery query = new StringQuery(queryString);

		assertThat(query.hasParameterBindings(), is(true));
		assertThat(query.getQueryString(), is(queryString));

		List<ParameterBinding> bindings = query.getParameterBindings();
		assertThat(bindings, hasSize(1));

		assertNamedBinding(InParameterBinding.class, "ids", bindings.get(0));
	}

	/**
	 * @see DATAJPA-461
	 */
	@Test
	public void detectsMultipleNamedInParameterBindings() {

		String queryString = "select u from User u where u.id in :ids and u.name in :names and foo = :bar";
		StringQuery query = new StringQuery(queryString);

		assertThat(query.hasParameterBindings(), is(true));
		assertThat(query.getQueryString(), is(queryString));

		List<ParameterBinding> bindings = query.getParameterBindings();
		assertThat(bindings, hasSize(3));

		assertNamedBinding(InParameterBinding.class, "ids", bindings.get(0));
		assertNamedBinding(InParameterBinding.class, "names", bindings.get(1));
		assertNamedBinding(ParameterBinding.class, "bar", bindings.get(2));
	}

	/**
	 * @see DATAJPA-461
	 */
	@Test
	public void detectsPositionalInParameterBindings() {

		String queryString = "select u from User u where u.id in ?1";
		StringQuery query = new StringQuery(queryString);

		assertThat(query.hasParameterBindings(), is(true));
		assertThat(query.getQueryString(), is(queryString));

		List<ParameterBinding> bindings = query.getParameterBindings();
		assertThat(bindings, hasSize(1));

		assertPositionalBinding(InParameterBinding.class, 1, bindings.get(0));
	}

	/**
	 * @see DATAJPA-461
	 */
	@Test
	public void detectsMultiplePositionalInParameterBindings() {

		String queryString = "select u from User u where u.id in ?1 and u.names in ?2 and foo = ?3";
		StringQuery query = new StringQuery(queryString);

		assertThat(query.hasParameterBindings(), is(true));
		assertThat(query.getQueryString(), is(queryString));

		List<ParameterBinding> bindings = query.getParameterBindings();
		assertThat(bindings, hasSize(3));

		assertPositionalBinding(InParameterBinding.class, 1, bindings.get(0));
		assertPositionalBinding(InParameterBinding.class, 2, bindings.get(1));
		assertPositionalBinding(ParameterBinding.class, 3, bindings.get(2));
	}

	/**
	 * @see DATAJPA-373
	 */
	@Test
	public void handlesMultipleNamedLikeBindingsCorrectly() {
		new StringQuery("select u from User u where u.firstname like %:firstname or foo like :bar");
	}

	/**
	 * @see DATAJPA-292, DATAJPA-362
	 */
	@Test(expected = IllegalArgumentException.class)
	public void rejectsDifferentBindingsForRepeatedParameter() {
		new StringQuery("select u from User u where u.firstname like %?1 and u.lastname like ?1%");
	}

	/**
	 * @see DATAJPA-461
	 */
	@Test
	public void treatsGreaterThanBindingAsSimpleBinding() {

		StringQuery query = new StringQuery("select u from User u where u.createdDate > ?1");
		List<ParameterBinding> bindings = query.getParameterBindings();

		assertThat(bindings, hasSize(1));
		assertPositionalBinding(ParameterBinding.class, 1, bindings.get(0));
	}

	/**
	 * @see DATAJPA-473
	 */
	@Test
	public void removesLikeBindingsFromQueryIfQueryContainsSimpleBinding() {

		StringQuery query = new StringQuery("SELECT a FROM Article a WHERE a.overview LIKE %:escapedWord% ESCAPE '~'"
				+ " OR a.content LIKE %:escapedWord% ESCAPE '~' OR a.title = :word ORDER BY a.articleId DESC");

		List<ParameterBinding> bindings = query.getParameterBindings();

		assertThat(bindings, hasSize(2));
		assertNamedBinding(LikeParameterBinding.class, "escapedWord", bindings.get(0));
		assertNamedBinding(ParameterBinding.class, "word", bindings.get(1));
		assertThat(query.getQueryString(), is("SELECT a FROM Article a WHERE a.overview LIKE :escapedWord ESCAPE '~'"
				+ " OR a.content LIKE :escapedWord ESCAPE '~' OR a.title = :word ORDER BY a.articleId DESC"));
	}

	/**
	 * @see DATAJPA-483
	 */
	@Test
	public void detectsInBindingWithParentheses() {

		StringQuery query = new StringQuery("select count(we) from MyEntity we where we.status in (:statuses)");

		List<ParameterBinding> bindings = query.getParameterBindings();

		assertThat(bindings, hasSize(1));
		assertNamedBinding(InParameterBinding.class, "statuses", bindings.get(0));
	}

	/**
	 * @see DATAJPA-513
	 */
	@Test
	public void rejectsNullParameterNameHintingTowardsAtParamForNullParameterName() {

		StringQuery query = new StringQuery("select x from X");

		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(Param.class.getSimpleName());

		query.getBindingFor(null);
	}

	/**
	 * @see DATAJPA-545
	 */
	@Test
	public void detectsInBindingWithSpecialFrenchCharactersInParentheses() {

		StringQuery query = new StringQuery("select * from MyEntity where abonnés in (:abonnés)");

		List<ParameterBinding> bindings = query.getParameterBindings();

		assertThat(bindings, hasSize(1));
		assertNamedBinding(InParameterBinding.class, "abonnés", bindings.get(0));
	}

	/**
	 * @see DATAJPA-545
	 */
	@Test
	public void detectsInBindingWithSpecialCharactersInParentheses() {

		StringQuery query = new StringQuery("select * from MyEntity where øre in (:øre)");

		List<ParameterBinding> bindings = query.getParameterBindings();

		assertThat(bindings, hasSize(1));
		assertNamedBinding(InParameterBinding.class, "øre", bindings.get(0));
	}

	/**
	 * @see DATAJPA-545
	 */
	@Test
	public void detectsInBindingWithSpecialAsianCharactersInParentheses() {

		StringQuery query = new StringQuery("select * from MyEntity where 생일 in (:생일)");

		List<ParameterBinding> bindings = query.getParameterBindings();

		assertThat(bindings, hasSize(1));
		assertNamedBinding(InParameterBinding.class, "생일", bindings.get(0));
	}

	/**
	 * @see DATAJPA-545
	 */
	@Test
	public void detectsInBindingWithSpecialCharactersAndWordCharactersMixedInParentheses() {

		StringQuery query = new StringQuery("select * from MyEntity where foo in (:ab1babc생일233)");

		List<ParameterBinding> bindings = query.getParameterBindings();

		assertThat(bindings, hasSize(1));
		assertNamedBinding(InParameterBinding.class, "ab1babc생일233", bindings.get(0));
	}

	/**
	 * @see DATAJPA-362
	 */
	@Test(expected = IllegalArgumentException.class)
	public void rejectsDifferentBindingsForRepeatedParameter2() {
		new StringQuery("select u from User u where u.firstname like ?1 and u.lastname like %?1");
	}
	
	/**
	 * @see @DATAJPA-712
	 */
	@Test
	public void shouldReplaceAllNamedExpressionParametersWithInClause() {
		
		StringQuery query = new StringQuery("select a from A a where a.b in :#{#bs} and a.c in :#{#cs}");
		String queryString = query.getQueryString();
		
		assertThat(queryString, is("select a from A a where a.b in :__$synthetic$__1 and a.c in :__$synthetic$__2"));
	}

	/**
	 * @see @DATAJPA-712
	 */
	@Test
	public void shouldReplaceAllPositionExpressionParametersWithInClause() {
		
		StringQuery query = new StringQuery("select a from A a where a.b in ?#{#bs} and a.c in ?#{#cs}");
		String queryString = query.getQueryString();
		
		assertThat(queryString, is("select a from A a where a.b in ?1 and a.c in ?2"));
	}
	
	private void assertPositionalBinding(Class<? extends ParameterBinding> bindingType, Integer position,
			ParameterBinding expectedBinding) {

		assertThat(bindingType.isInstance(expectedBinding), is(true));
		assertThat(expectedBinding, is(notNullValue()));
		assertThat(expectedBinding.hasPosition(position), is(true));
	}

	private void assertNamedBinding(Class<? extends ParameterBinding> bindingType, String parameterName,
			ParameterBinding expectedBinding) {

		assertThat(bindingType.isInstance(expectedBinding), is(true));
		assertThat(expectedBinding, is(notNullValue()));
		assertThat(expectedBinding.hasName(parameterName), is(true));
	}
}
