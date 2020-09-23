/*
 * Copyright 2008-2020 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.JpaQueryExecution.ModifyingExecution;
import org.springframework.data.jpa.repository.query.JpaQueryExecution.PagedExecution;

/**
 * Unit test for {@link JpaQueryExecution}.
 *
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Mark Paluch
 * @author Nicolas Cirigliano
 * @author Jens Schauder
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class JpaQueryExecutionUnitTests {

	@Mock EntityManager em;
	@Mock AbstractStringBasedJpaQuery jpaQuery;
	@Mock Query query;
	@Mock JpaQueryMethod method;
	@Mock JpaParametersParameterAccessor accessor;

	@Mock TypedQuery<Long> countQuery;

	public static void sampleMethod(Pageable pageable) {}

	@Before
	public void setUp() {

		when(query.executeUpdate()).thenReturn(0);
		when(jpaQuery.createQuery(Mockito.any(JpaParametersParameterAccessor.class))).thenReturn(query);
		when(jpaQuery.getQueryMethod()).thenReturn(method);
	}

	@Test
	public void rejectsNullQuery() {

		assertThatIllegalArgumentException().isThrownBy(() -> new StubQueryExecution().execute(null, accessor));
	}

	@Test
	public void rejectsNullBinder() {

		assertThatIllegalArgumentException().isThrownBy(() -> new StubQueryExecution().execute(jpaQuery, null));
	}

	@Test
	public void transformsNoResultExceptionToNull() {

		assertThat(new JpaQueryExecution() {

			@Override
			protected Object doExecute(AbstractJpaQuery query, JpaParametersParameterAccessor accessor) {

				return null;
			}
		}.execute(jpaQuery, accessor)).isNull();
	}

	@Test // DATAJPA-806
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void modifyingExecutionFlushesEntityManagerIfSet() {

		when(method.getReturnType()).thenReturn((Class) void.class);
		when(method.getFlushAutomatically()).thenReturn(true);

		ModifyingExecution execution = new ModifyingExecution(method, em);
		execution.execute(jpaQuery, accessor);

		verify(em, times(1)).flush();
		verify(em, times(0)).clear();
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void modifyingExecutionClearsEntityManagerIfSet() {

		when(method.getReturnType()).thenReturn((Class) void.class);
		when(method.getClearAutomatically()).thenReturn(true);

		ModifyingExecution execution = new ModifyingExecution(method, em);
		execution.execute(jpaQuery, accessor);

		verify(em, times(0)).flush();
		verify(em, times(1)).clear();
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void allowsMethodReturnTypesForModifyingQuery() {

		when(method.getReturnType()).thenReturn((Class) void.class, (Class) int.class, (Class) Integer.class);

		new ModifyingExecution(method, em);
		new ModifyingExecution(method, em);
		new ModifyingExecution(method, em);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void modifyingExecutionRejectsNonIntegerOrVoidReturnType() {

		when(method.getReturnType()).thenReturn((Class) Long.class);
		assertThatIllegalArgumentException().isThrownBy(() -> new ModifyingExecution(method, em));
	}

	@Test // DATAJPA-124, DATAJPA-912
	public void pagedExecutionRetrievesObjectsForPageableOutOfRange() throws Exception {

		JpaParameters parameters = new JpaParameters(getClass().getMethod("sampleMethod", Pageable.class));
		when(jpaQuery.createCountQuery(Mockito.any())).thenReturn(countQuery);
		when(jpaQuery.createQuery(Mockito.any())).thenReturn(query);
		when(countQuery.getResultList()).thenReturn(Arrays.asList(20L));

		PagedExecution execution = new PagedExecution();
		execution.doExecute(jpaQuery,
				new JpaParametersParameterAccessor(parameters, new Object[] { PageRequest.of(2, 10) }));

		verify(query).getResultList();
		verify(countQuery).getResultList();
	}

	@Test // DATAJPA-477, DATAJPA-912
	public void pagedExecutionShouldNotGenerateCountQueryIfQueryReportedNoResults() throws Exception {

		JpaParameters parameters = new JpaParameters(getClass().getMethod("sampleMethod", Pageable.class));
		when(jpaQuery.createQuery(Mockito.any())).thenReturn(query);
		when(query.getResultList()).thenReturn(Arrays.asList(0L));

		PagedExecution execution = new PagedExecution();
		execution.doExecute(jpaQuery,
				new JpaParametersParameterAccessor(parameters, new Object[] { PageRequest.of(0, 10) }));

		verify(countQuery, times(0)).getResultList();
		verify(jpaQuery, times(0)).createCountQuery(any());
	}

	@Test // DATAJPA-912
	public void pagedExecutionShouldUseCountFromResultIfOffsetIsZeroAndResultsWithinPageSize() throws Exception {

		JpaParameters parameters = new JpaParameters(getClass().getMethod("sampleMethod", Pageable.class));
		when(jpaQuery.createQuery(Mockito.any())).thenReturn(query);
		when(query.getResultList()).thenReturn(Arrays.asList(new Object(), new Object(), new Object(), new Object()));

		PagedExecution execution = new PagedExecution();
		execution.doExecute(jpaQuery,
				new JpaParametersParameterAccessor(parameters, new Object[] { PageRequest.of(0, 10) }));

		verify(jpaQuery, times(0)).createCountQuery(any());
	}

	@Test // DATAJPA-912
	public void pagedExecutionShouldUseCountFromResultWithOffsetAndResultsWithinPageSize() throws Exception {

		JpaParameters parameters = new JpaParameters(getClass().getMethod("sampleMethod", Pageable.class));
		when(jpaQuery.createQuery(Mockito.any())).thenReturn(query);
		when(query.getResultList()).thenReturn(Arrays.asList(new Object(), new Object(), new Object(), new Object()));

		PagedExecution execution = new PagedExecution();
		execution.doExecute(jpaQuery,
				new JpaParametersParameterAccessor(parameters, new Object[] { PageRequest.of(5, 10) }));

		verify(jpaQuery, times(0)).createCountQuery(any());
	}

	@Test // DATAJPA-912
	public void pagedExecutionShouldUseRequestCountFromResultWithOffsetAndResultsHitLowerPageSizeBounds()
			throws Exception {

		JpaParameters parameters = new JpaParameters(getClass().getMethod("sampleMethod", Pageable.class));
		when(jpaQuery.createQuery(Mockito.any())).thenReturn(query);
		when(query.getResultList()).thenReturn(Collections.emptyList());
		when(jpaQuery.createCountQuery(Mockito.any())).thenReturn(query);
		when(countQuery.getResultList()).thenReturn(Arrays.asList(20L));

		PagedExecution execution = new PagedExecution();
		execution.doExecute(jpaQuery,
				new JpaParametersParameterAccessor(parameters, new Object[] { PageRequest.of(4, 4) }));

		verify(jpaQuery).createCountQuery(any());
	}

	@Test // DATAJPA-912
	public void pagedExecutionShouldUseRequestCountFromResultWithOffsetAndResultsHitUpperPageSizeBounds()
			throws Exception {

		JpaParameters parameters = new JpaParameters(getClass().getMethod("sampleMethod", Pageable.class));
		when(jpaQuery.createQuery(Mockito.any())).thenReturn(query);
		when(query.getResultList()).thenReturn(Arrays.asList(new Object(), new Object(), new Object(), new Object()));
		when(jpaQuery.createCountQuery(Mockito.any())).thenReturn(query);
		when(countQuery.getResultList()).thenReturn(Arrays.asList(20L));

		PagedExecution execution = new PagedExecution();
		execution.doExecute(jpaQuery,
				new JpaParametersParameterAccessor(parameters, new Object[] { PageRequest.of(4, 4) }));

		verify(jpaQuery).createCountQuery(any());
	}

	@Test // DATAJPA-951
	public void doesNotPreemtivelyWrapResultIntoOptional() throws Exception {

		doReturn(method).when(jpaQuery).getQueryMethod();
		doReturn(Optional.class).when(method).getReturnType();
		JpaParametersParameterAccessor accessor = mock(JpaParametersParameterAccessor.class);

		StubQueryExecution execution = new StubQueryExecution() {
			@Override
			protected Object doExecute(AbstractJpaQuery query, JpaParametersParameterAccessor accessor) {
				return "result";
			}
		};

		Object result = execution.execute(jpaQuery, accessor);

		assertThat(result).isInstanceOf(String.class);
	}

	static class StubQueryExecution extends JpaQueryExecution {

		@Override
		protected Object doExecute(AbstractJpaQuery query, JpaParametersParameterAccessor accessor) {
			return null;
		}
	}
}
