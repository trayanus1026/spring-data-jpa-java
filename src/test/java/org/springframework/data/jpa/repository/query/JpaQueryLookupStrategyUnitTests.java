/*
 * Copyright 2012-2013 the original author or authors.
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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.jpa.domain.sample.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;

/**
 * Unit tests for {@link JpaQueryLookupStrategy}.
 * 
 * @author Oliver Gierke
 */
@RunWith(MockitoJUnitRunner.class)
public class JpaQueryLookupStrategyUnitTests {

	@Mock EntityManager em;
	@Mock EntityManagerFactory emf;
	@Mock QueryExtractor extractor;
	@Mock NamedQueries namedQueries;

	@Before
	public void setUp() {
		when(em.getEntityManagerFactory()).thenReturn(emf);
		when(emf.createEntityManager()).thenReturn(em);
	}

	/**
	 * @see DATAJPA-226
	 */
	@Test
	public void invalidAnnotatedQueryCausesException() throws Exception {

		QueryLookupStrategy strategy = JpaQueryLookupStrategy.create(em, Key.CREATE_IF_NOT_FOUND, extractor);
		Method method = UserRepository.class.getMethod("findByFoo", String.class);
		RepositoryMetadata metadata = new DefaultRepositoryMetadata(UserRepository.class);

		Throwable reference = new RuntimeException();
		when(em.createQuery(anyString())).thenThrow(reference);

		try {
			strategy.resolveQuery(method, metadata, namedQueries);
		} catch (Exception e) {
			assertThat(e, is(instanceOf(IllegalArgumentException.class)));
			assertThat(e.getCause(), is(reference));
		}
	}

	interface UserRepository extends Repository<User, Long> {

		@Query("something absurd")
		User findByFoo(String foo);
	}
}
