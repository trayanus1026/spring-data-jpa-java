/*
 * Copyright 2008-2018 the original author or authors.
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;
import org.springframework.data.util.BeanLookup;
import org.springframework.data.util.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Special adapter for Springs {@link org.springframework.beans.factory.FactoryBean} interface to allow easy setup of
 * repository factories via Spring configuration.
 *
 * @author Oliver Gierke
 * @author Eberhard Wolff
 * @author Mark Paluch
 * @author Jens Schauder
 * @param <T> the type of the repository
 */
public class JpaRepositoryFactoryBean<T extends Repository<S, ID>, S, ID>
		extends TransactionalRepositoryFactoryBeanSupport<T, S, ID> {

	private @Nullable EntityManager entityManager;
	private Lazy<EntityPathResolver> entityPathResolver = Lazy.of(SimpleEntityPathResolver.INSTANCE);

	/**
	 * Creates a new {@link JpaRepositoryFactoryBean} for the given repository interface.
	 *
	 * @param repositoryInterface must not be {@literal null}.
	 */
	public JpaRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
		super(repositoryInterface);
	}

	/**
	 * The {@link EntityManager} to be used.
	 *
	 * @param entityManager the entityManager to set
	 */
	@PersistenceContext
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport#setMappingContext(org.springframework.data.mapping.context.MappingContext)
	 */
	@Override
	public void setMappingContext(MappingContext<?, ?> mappingContext) {
		super.setMappingContext(mappingContext);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport#doCreateRepositoryFactory()
	 */
	@Override
	protected RepositoryFactorySupport doCreateRepositoryFactory() {

		Assert.state(entityManager != null, "EntityManager must not be null!");

		return createRepositoryFactory(entityManager);
	}

	/**
	 * Returns a {@link RepositoryFactorySupport}.
	 */
	protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {

		EntityPathResolver resolver = entityPathResolver.get();

		JpaRepositoryFactory jpaRepositoryFactory = new JpaRepositoryFactory(entityManager);
		jpaRepositoryFactory.setEntityPathResolver(resolver);

		return jpaRepositoryFactory;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {

		Assert.state(entityManager != null, "EntityManager must not be null!");

		super.afterPropertiesSet();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	public void setBeanFactory(BeanFactory beanFactory) {

		Assert.isInstanceOf(ListableBeanFactory.class, beanFactory);

		super.setBeanFactory(beanFactory);

		this.entityPathResolver = BeanLookup.lazyIfAvailable(EntityPathResolver.class, beanFactory) //
				.or(SimpleEntityPathResolver.INSTANCE);
	}
}
