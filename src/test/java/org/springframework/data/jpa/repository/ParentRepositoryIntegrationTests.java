/*
 * Copyright 2013-2021 the original author or authors.
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
package org.springframework.data.jpa.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.sample.Child;
import org.springframework.data.jpa.domain.sample.Parent;
import org.springframework.data.jpa.repository.sample.ParentRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Jens Schauder
 */
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:config/namespace-application-context.xml")
public class ParentRepositoryIntegrationTests {

	@Autowired ParentRepository repository;

	@BeforeEach
	void setUp() {

		repository.save(new Parent().add(new Child()));
		repository.save(new Parent().add(new Child()).add(new Child()));
		repository.save(new Parent().add(new Child()));
		repository.save(new Parent());
		repository.flush();
	}

	@Test // DATAJPA-287
	void testWithoutJoin() {

		Page<Parent> page = repository.findAll(new Specification<Parent>() {
			@Override
			public Predicate toPredicate(Root<Parent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Path<Set<Child>> childrenPath = root.get("children");
				query.distinct(true);
				return cb.isNotEmpty(childrenPath);
			}
		}, PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "id")));

		List<Parent> content = page.getContent();

		assertThat(content.size()).isEqualTo(3);
		assertThat(page.getSize()).isEqualTo(5);
		assertThat(page.getNumber()).isEqualTo(0);
		assertThat(page.getTotalElements()).isEqualTo(3L);
		assertThat(page.getTotalPages()).isEqualTo(1);
	}

	@Test // DATAJPA-287
	void testWithJoin() throws Exception {
		Page<Parent> page = repository.findAll(new Specification<Parent>() {
			@Override
			public Predicate toPredicate(Root<Parent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				root.join("children");
				// we are interesting in distinct items, especially when join presents in query
				query.distinct(true);
				return cb.isNotEmpty(root.<Set<Child>> get("children"));
			}
		}, PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "id")));

		List<Parent> content = page.getContent();

		// according to the initial setup there should be
		// 3 parents which children collection is not empty
		assertThat(content.size()).isEqualTo(3);
		assertThat(page.getSize()).isEqualTo(5);
		assertThat(page.getNumber()).isEqualTo(0);

		// we get here wrong total elements number since
		// count query doesn't take into account the distinct marker of query
		assertThat(page.getTotalElements()).isEqualTo(3L);
		assertThat(page.getTotalPages()).isEqualTo(1);
	}
}
