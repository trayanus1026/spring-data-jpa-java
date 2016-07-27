/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.data.jpa.convert;

import static org.hamcrest.core.IsEqual.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.data.domain.Example.*;

import java.lang.reflect.Member;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Id;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.util.ObjectUtils;

/**
 * Unit tests for {@link QueryByExamplePredicateBuilder}.
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 */
@RunWith(MockitoJUnitRunner.class)
public class QueryByExamplePredicateBuilderUnitTests {

	@Mock CriteriaBuilder cb;
	@Mock Root root;
	@Mock EntityType<Person> personEntityType;
	@Mock Expression expressionMock;
	@Mock Predicate truePredicate;
	@Mock Predicate dummyPredicate;
	@Mock Predicate listPredicate;
	@Mock Path dummyPath;

	Set<SingularAttribute<? super Person, ?>> personEntityAttribtues;

	SingularAttribute<? super Person, Long> personIdAttribute;
	SingularAttribute<? super Person, String> personFirstnameAttribute;
	SingularAttribute<? super Person, Long> personAgeAttribute;
	SingularAttribute<? super Person, Person> personFatherAttribute;
	SingularAttribute<? super Person, Skill> personSkillAttribute;
	SingularAttribute<? super Person, Address> personAddressAttribute;

	public @Rule ExpectedException exception = ExpectedException.none();

	@Before
	public void setUp() {

		personIdAttribute = new SingluarAttributeStub<Person, Long>("id", PersistentAttributeType.BASIC, Long.class);
		personFirstnameAttribute = new SingluarAttributeStub<Person, String>("firstname", PersistentAttributeType.BASIC,
				String.class);
		personAgeAttribute = new SingluarAttributeStub<Person, Long>("age", PersistentAttributeType.BASIC, Long.class);
		personFatherAttribute = new SingluarAttributeStub<Person, Person>("father", PersistentAttributeType.MANY_TO_ONE,
				Person.class, personEntityType);
		personSkillAttribute = new SingluarAttributeStub<Person, Skill>("skill", PersistentAttributeType.MANY_TO_ONE,
				Skill.class);
		personAddressAttribute = new SingluarAttributeStub<Person, Address>("address", PersistentAttributeType.EMBEDDED,
				Address.class);

		personEntityAttribtues = new LinkedHashSet<SingularAttribute<? super Person, ?>>();
		personEntityAttribtues.add(personIdAttribute);
		personEntityAttribtues.add(personFirstnameAttribute);
		personEntityAttribtues.add(personAgeAttribute);
		personEntityAttribtues.add(personFatherAttribute);
		personEntityAttribtues.add(personAddressAttribute);
		personEntityAttribtues.add(personSkillAttribute);

		when(root.get(any(SingularAttribute.class))).thenReturn(dummyPath);
		when(root.getModel()).thenReturn(personEntityType);
		when(personEntityType.getSingularAttributes()).thenReturn(personEntityAttribtues);

		when(cb.equal(any(Expression.class), any(String.class))).thenReturn(dummyPredicate);
		when(cb.equal(any(Expression.class), any(Long.class))).thenReturn(dummyPredicate);
		when(cb.like(any(Expression.class), any(String.class))).thenReturn(dummyPredicate);

		when(cb.literal(any(Boolean.class))).thenReturn(expressionMock);
		when(cb.isTrue(eq(expressionMock))).thenReturn(truePredicate);
		when(cb.and(Matchers.<Predicate> anyVararg())).thenReturn(listPredicate);
	}

	/**
	 * @see DATAJPA-218
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getPredicateShouldThrowExceptionOnNullRoot() {
		QueryByExamplePredicateBuilder.getPredicate(null, cb, of(new Person()));
	}

	/**
	 * @see DATAJPA-218
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getPredicateShouldThrowExceptionOnNullCriteriaBuilder() {
		QueryByExamplePredicateBuilder.getPredicate(root, null, of(new Person()));
	}

	/**
	 * @see DATAJPA-218
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getPredicateShouldThrowExceptionOnNullExample() {
		QueryByExamplePredicateBuilder.getPredicate(root, null, null);
	}

	/**
	 * @see DATAJPA-218
	 */
	@Test
	public void emptyCriteriaListShouldResultTruePredicate() {
		assertThat(QueryByExamplePredicateBuilder.getPredicate(root, cb, of(new Person())), equalTo(truePredicate));
	}

	/**
	 * @see DATAJPA-218
	 */
	@Test
	public void singleElementCriteriaShouldJustReturnIt() {

		Person p = new Person();
		p.firstname = "foo";

		assertThat(QueryByExamplePredicateBuilder.getPredicate(root, cb, of(p)), equalTo(dummyPredicate));
		verify(cb, times(1)).equal(any(Expression.class), eq("foo"));
	}

	/**
	 * @see DATAJPA-937
	 */
	@Test
	public void unresolvableNestedAssociatedPathShouldFail() {

		Person p = new Person();
		Person father = new Person();
		father.father = new Person();
		p.father = father;

		exception.expectCause(IsInstanceOf.<Throwable> instanceOf(IllegalArgumentException.class));
		exception.expectMessage("Unexpected path type");

		QueryByExamplePredicateBuilder.getPredicate(root, cb, of(p));
	}

	/**
	 * @see DATAJPA-218
	 */
	@Test
	public void multiPredicateCriteriaShouldReturnCombinedOnes() {

		Person p = new Person();
		p.firstname = "foo";
		p.age = 2L;

		assertThat(QueryByExamplePredicateBuilder.getPredicate(root, cb, of(p)), equalTo(listPredicate));

		verify(cb, times(1)).equal(any(Expression.class), eq("foo"));
		verify(cb, times(1)).equal(any(Expression.class), eq(2L));
	}

	static class Person {

		@Id Long id;
		String firstname;
		Long age;

		Person father;
		Address address;
		Skill skill;
	}

	static class Address {

		String city;
		String country;
	}

	static class Skill {

		@Id Long id;
		String name;
	}

	static class SingluarAttributeStub<X, T> implements SingularAttribute<X, T> {

		private String name;
		private PersistentAttributeType attributeType;
		private Class<T> javaType;
		private Type<T> type;

		public SingluarAttributeStub(String name,
				javax.persistence.metamodel.Attribute.PersistentAttributeType attributeType, Class<T> javaType) {
			this(name, attributeType, javaType, null);
		}

		public SingluarAttributeStub(String name,
				javax.persistence.metamodel.Attribute.PersistentAttributeType attributeType, Class<T> javaType, Type<T> type) {
			this.name = name;
			this.attributeType = attributeType;
			this.javaType = javaType;
			this.type = type;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public javax.persistence.metamodel.Attribute.PersistentAttributeType getPersistentAttributeType() {
			return attributeType;
		}

		@Override
		public ManagedType<X> getDeclaringType() {
			return null;
		}

		@Override
		public Class<T> getJavaType() {
			return javaType;
		}

		@Override
		public Member getJavaMember() {
			return null;
		}

		@Override
		public boolean isAssociation() {
			return !attributeType.equals(PersistentAttributeType.BASIC)
					&& !attributeType.equals(PersistentAttributeType.EMBEDDED);
		}

		@Override
		public boolean isCollection() {
			return false;
		}

		@Override
		public javax.persistence.metamodel.Bindable.BindableType getBindableType() {
			return BindableType.SINGULAR_ATTRIBUTE;
		}

		@Override
		public Class<T> getBindableJavaType() {
			return javaType;
		}

		@Override
		public boolean isId() {
			return ObjectUtils.nullSafeEquals(name, "id");
		}

		@Override
		public boolean isVersion() {
			return false;
		}

		@Override
		public boolean isOptional() {
			return false;
		}

		@Override
		public Type<T> getType() {
			return type;
		}

	}
}
