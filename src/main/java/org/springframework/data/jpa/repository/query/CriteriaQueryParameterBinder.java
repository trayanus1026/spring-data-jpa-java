/*
 * Copyright 2011-2013 the original author or authors.
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

import java.util.Date;
import java.util.Iterator;

import javax.persistence.Parameter;
import javax.persistence.Query;

import org.springframework.data.jpa.repository.query.JpaParameters.JpaParameter;
import org.springframework.data.jpa.repository.query.ParameterMetadataProvider.ParameterMetadata;
import org.springframework.data.jpa.repository.support.ExpressionEvaluationContextProvider;
import org.springframework.data.repository.query.Parameters;
import org.springframework.util.Assert;

/**
 * Special {@link ParameterBinder} that uses {@link javax.persistence.criteria.ParameterExpression}s to bind query
 * parameters.
 * 
 * @author Oliver Gierke
 * @author Thomas Darimont
 */
class CriteriaQueryParameterBinder extends ExpressionAwareParameterBinder {

	private final Iterator<ParameterMetadata<?>> expressions;

	/**
	 * Creates a new {@link CriteriaQueryParameterBinder} for the given {@link Parameters}, values and some
	 * {@link javax.persistence.criteria.ParameterExpression}.
	 * 
	 * @param parameters
	 * @param values
	 * @param expressions
	 * @param evaluationContextProvider
	 */
	CriteriaQueryParameterBinder(JpaParameters parameters, Object[] values, Iterable<ParameterMetadata<?>> expressions,
			ExpressionEvaluationContextProvider evaluationContextProvider) {

		super(parameters, values, evaluationContextProvider);
		Assert.notNull(expressions);
		this.expressions = expressions.iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.jpa.repository.query.ParameterBinder#bind(javax.persistence.Query, org.springframework.data.jpa.repository.query.JpaParameters.JpaParameter, java.lang.Object, int)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void bind(Query query, JpaParameter parameter, Object value, int position) {

		ParameterMetadata<Object> metadata = (ParameterMetadata<Object>) expressions.next();

		if (metadata.isIsNullParameter()) {
			return;
		}

		if (parameter.isTemporalParameter()) {
			query.setParameter((Parameter<Date>) (Object) metadata.getExpression(), (Date) metadata.prepare(value),
					parameter.getTemporalType());
		} else {
			query.setParameter(metadata.getExpression(), metadata.prepare(value));
		}
	}
}
