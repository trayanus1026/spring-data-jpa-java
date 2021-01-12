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
package org.springframework.data.jpa.repository.support;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.Collections;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Unit tests for {@link AbstractJpaEntityInformation}.
 *
 * @author Oliver Gierke
 * @author Jens Schauder
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class JpaEntityInformationSupportUnitTests {

	@Mock EntityManager em;
	@Mock Metamodel metaModel;

	@Test
	void usesSimpleClassNameIfNoEntityNameGiven() throws Exception {

		JpaEntityInformation<User, Integer> information = new DummyJpaEntityInformation<>(User.class);
		assertThat(information.getEntityName()).isEqualTo("User");

		JpaEntityInformation<NamedUser, ?> second = new DummyJpaEntityInformation<NamedUser, Serializable>(NamedUser.class);
		assertThat(second.getEntityName()).isEqualTo("AnotherNamedUser");
	}

	@Test // DATAJPA-93
	void rejectsClassNotBeingFoundInMetamodel() {

		when(em.getMetamodel()).thenReturn(metaModel);
		assertThatIllegalArgumentException()
				.isThrownBy(() -> JpaEntityInformationSupport.getEntityInformation(User.class, em));
	}

	static class User {

	}

	static class DummyJpaEntityInformation<T, ID> extends JpaEntityInformationSupport<T, ID> {

		DummyJpaEntityInformation(Class<T> domainClass) {
			super(domainClass);
		}

		@Override
		public SingularAttribute<? super T, ?> getIdAttribute() {
			return null;
		}

		@Override
		public ID getId(T entity) {
			return null;
		}

		@Override
		public Class<ID> getIdType() {
			return null;
		}

		@Override
		public Iterable<String> getIdAttributeNames() {
			return Collections.emptySet();
		}

		@Override
		public boolean hasCompositeId() {
			return false;
		}

		@Override
		public Object getCompositeIdAttributeValue(Object id, String idAttribute) {
			return null;
		}
	}

	@Entity(name = "AnotherNamedUser")
	public class NamedUser {

	}
}
