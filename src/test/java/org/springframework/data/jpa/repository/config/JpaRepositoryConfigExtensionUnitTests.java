/*
 * Copyright 2013 the original author or authors.
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
package org.springframework.data.jpa.repository.config;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor;

/**
 * Unit tests for {@link JpaRepositoryConfigExtension}.
 * 
 * @author Oliver Gierke
 */
@RunWith(MockitoJUnitRunner.class)
public class JpaRepositoryConfigExtensionUnitTests {

	@Mock
	RepositoryConfigurationSource configSource;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void registersDefaultBeanPostProcessorsByDefault() {

		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();

		RepositoryConfigurationExtension extension = new JpaRepositoryConfigExtension();
		extension.registerBeansForRoot(factory, configSource);

		Iterable<String> names = Arrays.asList(factory.getBeanDefinitionNames());

		assertThat(names, hasItem(startsWith("org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor")));
		assertThat(names,
				hasItem(startsWith("org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor")));
		assertThat(
				names,
				hasItem(startsWith("org.springframework.data.repository.core.support.RepositoryInterfaceAwareBeanPostProcessor")));
	}

	@Test
	public void doesNotRegisterProcessorIfAlreadyPresent() {

		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		RootBeanDefinition pabppDefinition = new RootBeanDefinition(PersistenceAnnotationBeanPostProcessor.class);
		String beanName = BeanDefinitionReaderUtils.generateBeanName(pabppDefinition, factory);
		factory.registerBeanDefinition(beanName, pabppDefinition);

		assertOnlyOnePersistenceAnnotationBeanPostProcessorRegistered(factory, beanName);
	}

	@Test
	public void doesNotRegisterProcessorIfAutoRegistered() {

		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		RootBeanDefinition pabppDefinition = new RootBeanDefinition(PersistenceAnnotationBeanPostProcessor.class);
		String beanName = AnnotationConfigUtils.PERSISTENCE_ANNOTATION_PROCESSOR_BEAN_NAME;
		factory.registerBeanDefinition(beanName, pabppDefinition);

		assertOnlyOnePersistenceAnnotationBeanPostProcessorRegistered(factory, beanName);
	}

	private void assertOnlyOnePersistenceAnnotationBeanPostProcessorRegistered(DefaultListableBeanFactory factory,
			String expectedBeanName) {

		RepositoryConfigurationExtension extension = new JpaRepositoryConfigExtension();
		extension.registerBeansForRoot(factory, configSource);

		assertThat(factory.getBean(expectedBeanName), is(notNullValue()));
		exception.expect(NoSuchBeanDefinitionException.class);
		factory.getBeanDefinition("org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor#1");
	}
}
