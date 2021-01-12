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
package org.springframework.data.jpa.repository.sample;

import org.springframework.data.jpa.domain.sample.User;
import org.springframework.data.repository.Repository;

/**
 * @author Thomas Darimont
 * @author Oliver Gierke
 */
public class ClassWithNestedRepository {

	public static interface NestedUserRepository extends Repository<User, Integer> {}
}
