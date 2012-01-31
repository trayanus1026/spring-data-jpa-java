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
package org.springframework.data.jpa.repository.support;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.jpa.domain.sample.SampleEntity;
import org.springframework.data.jpa.domain.sample.SampleEntityPK;
import org.springframework.data.jpa.domain.sample.SampleWithIdClass;
import org.springframework.data.jpa.domain.sample.SampleWithIdClassPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test for {@link JpaRepository}.
 * 
 * @author Oliver Gierke
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:infrastructure.xml" })
@Transactional
public class JpaRepositoryTests {

	@PersistenceContext
	EntityManager em;

	JpaRepository<SampleEntity, SampleEntityPK> repository;
	CrudRepository<SampleWithIdClass, SampleWithIdClassPK> idClassRepository;

	@Before
	public void setUp() {

		repository = new JpaRepositoryFactory(em).getRepository(SampleEntityRepository.class);
		idClassRepository = new JpaRepositoryFactory(em).getRepository(SampleWithIdClassRepository.class);
	}

	@Test
	public void testCrudOperationsForCompoundKeyEntity() throws Exception {

		SampleEntity entity = new SampleEntity("foo", "bar");
		repository.saveAndFlush(entity);
		assertThat(repository.exists(new SampleEntityPK("foo", "bar")), is(true));
		assertThat(repository.count(), is(1L));
		assertThat(repository.findOne(new SampleEntityPK("foo", "bar")), is(entity));

		repository.delete(Arrays.asList(entity));
		repository.flush();
		assertThat(repository.count(), is(0L));
	}

	/**
	 * DATAJPA-50
	 */
	@Test
	public void executesCrudOperationsForEntityWithIdClass() {

		SampleWithIdClass entity = new SampleWithIdClass(1L, 1L);
		idClassRepository.save(entity);

		assertThat(entity.getFirst(), is(notNullValue()));
		assertThat(entity.getSecond(), is(notNullValue()));

		SampleWithIdClassPK id = new SampleWithIdClassPK(entity.getFirst(), entity.getSecond());

		assertThat(idClassRepository.findOne(id), is(entity));
	}

	private static interface SampleEntityRepository extends JpaRepository<SampleEntity, SampleEntityPK> {

	}

	private static interface SampleWithIdClassRepository extends CrudRepository<SampleWithIdClass, SampleWithIdClassPK> {

	}
}
