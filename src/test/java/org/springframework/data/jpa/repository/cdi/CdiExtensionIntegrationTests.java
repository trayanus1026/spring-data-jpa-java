/*
 * Copyright 2011-2021 the original author or authors.
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
package org.springframework.data.jpa.repository.cdi;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.enterprise.inject.spi.Bean;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration tests for Spring Data JPA CDI extension.
 *
 * @author Dirk Mahler
 * @author Oliver Gierke
 * @author Mark Paluch
 * @author Jens Schauder
 */
class CdiExtensionIntegrationTests {

	private static SeContainer container;
	private static Logger LOGGER = LoggerFactory.getLogger(CdiExtensionIntegrationTests.class);

	@BeforeAll
	static void setUp() {

		container = SeContainerInitializer.newInstance() //
				.disableDiscovery() //
				.addPackages(PersonRepository.class) //
				.initialize();

		LOGGER.debug("CDI container bootstrapped!");
	}

	@Test // DATAJPA-319, DATAJPA-1180
	@SuppressWarnings("rawtypes")
	void foo() {

		Set<Bean<?>> beans = container.getBeanManager().getBeans(PersonRepository.class);

		assertThat(beans).hasSize(1);
		assertThat(beans.iterator().next().getScope()).isEqualTo((Class) ApplicationScoped.class);
	}

	@Test // DATAJPA-136, DATAJPA-1180
	void saveAndFindAll() {

		RepositoryConsumer repositoryConsumer = container.select(RepositoryConsumer.class).get();

		Person person = new Person();
		repositoryConsumer.save(person);
		repositoryConsumer.findAll();
	}

	@Test // DATAJPA-584, DATAJPA-1180
	void returnOneFromCustomImpl() {

		RepositoryConsumer repositoryConsumer = container.select(RepositoryConsumer.class).get();
		assertThat(repositoryConsumer.returnOne()).isEqualTo(1);
	}

	@Test // DATAJPA-584, DATAJPA-1180
	void useQualifiedCustomizedUserRepo() {

		RepositoryConsumer repositoryConsumer = container.select(RepositoryConsumer.class).get();
		repositoryConsumer.doSomethingOnUserDB();
	}

	@Test // DATAJPA-1287
	void useQualifiedFragmentUserRepo() {

		RepositoryConsumer repositoryConsumer = container.select(RepositoryConsumer.class).get();
		assertThat(repositoryConsumer.returnOneUserDB()).isEqualTo(1);
	}
}
