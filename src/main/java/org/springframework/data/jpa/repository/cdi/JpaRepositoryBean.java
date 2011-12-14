/*
 * Copyright 2011 the original author or authors.
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
package org.springframework.data.jpa.repository.cdi;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.cdi.CdiRepositoryBean;
import org.springframework.util.Assert;

/**
 * A bean which represents a JPA repository.
 * 
 * @author Dirk Mahler
 * @author Oliver Gierke
 * 
 * @param <T> The type of the repository.
 */
class JpaRepositoryBean<T> extends CdiRepositoryBean<T> {

	private final Bean<EntityManager> entityManagerBean;

	/**
	 * Constructs a {@link JpaRepositoryBean}.
	 * 
	 * @param beanManager must not be {@literal null}.
	 * @param entityManagerBean must not be {@literal null}.
	 * @param qualifiers must not be {@literal null}.
	 * @param repositoryType must not be {@literal null}.
	 */
	JpaRepositoryBean(BeanManager beanManager, Bean<EntityManager> entityManagerBean, Set<Annotation> qualifiers,
			Class<T> repositoryType) {

		super(qualifiers, repositoryType, beanManager);

		Assert.notNull(entityManagerBean);
		this.entityManagerBean = entityManagerBean;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.enterprise.context.spi.Contextual#create(javax.enterprise .context.spi.CreationalContext)
	 */
	@Override
	public T create(CreationalContext<T> creationalContext, Class<T> repositoryType) {

		// Get an instance from the associated entity manager bean.
		EntityManager entityManager = getDependencyInstance(entityManagerBean, EntityManager.class);
		// Create the JPA repository instance and return it.
		JpaRepositoryFactory factory = new JpaRepositoryFactory(entityManager);
		return factory.getRepository(repositoryType);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.enterprise.inject.spi.Bean#getScope()
	 */
	public Class<? extends Annotation> getScope() {

		// The repository uses the same scope as the associated EntityManager.
		return entityManagerBean.getScope();
	}
}