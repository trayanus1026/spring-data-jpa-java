/*
 * Copyright 2018-2020 the original author or authors.
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
package org.springframework.data.jpa.repository.support;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link DefaultQueryHints}.
 *
 * @author Jens Schauder
 */
public class DefaultQueryHintsTest {

	JpaEntityInformation<?, ?> information = mock(JpaEntityInformation.class);
	CrudMethodMetadata metadata = mock(CrudMethodMetadata.class);

	@Before
	public void before() {

		setupMainHints();
		setUpCountHints();
	}

	@Test // DATAJPA-1156
	public void mainHints() {

		QueryHints hints = DefaultQueryHints.of(information, metadata);

		Map<String, Object> collectedHints=new HashMap<>();
		hints.forEach(collectedHints::put);

		assertThat(collectedHints) //
				.contains( //
						entry("name1", "value1"), //
						entry("name2", "value2") //
				);
	}

	@Test // DATAJPA-1156
	public void countHints() {

		QueryHints hints = DefaultQueryHints.of(information, metadata).forCounts();

		Map<String, Object> collectedHints=new HashMap<>();
		hints.forEach(collectedHints::put);

		assertThat(collectedHints) //
				.contains( //
						entry("n1", "1"), //
						entry("n2", "2") //
				);
	}

	private void setupMainHints() {

		SimpleQueryHints mainHints = new SimpleQueryHints();
		mainHints.add("name1", "value1");
		mainHints.add("name2", "value2");

		when(metadata.getQueryHints()).thenReturn(mainHints);
	}

	private void setUpCountHints() {

		SimpleQueryHints countHints = new SimpleQueryHints();
		countHints.add("n1", "1");
		countHints.add("n2", "2");

		when(metadata.getQueryHintsForCount()).thenReturn(countHints);
	}
}
