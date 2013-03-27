/*
 * Copyright 2013 JBoss by Red Hat.
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
package org.kie.internal.runtime.manager.cdi;

import java.util.Set;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;

/**
 *
 * @author salaboy
 */
public class RuntimeManagedBeanProducer {
    public static final ThreadLocal<String> managedInstanceId = new ThreadLocal<String>();

	@RuntimeManagedBeanInstance
	@Produces
	public RuntimeManagedBean create(InjectionPoint ip, BeanManager bm) {
		//Get the Instance annotation value
		RuntimeManagedBeanInstance instance = ip.getAnnotated().getAnnotation(RuntimeManagedBeanInstance.class);
		String id = instance.value();
		Set<Bean<?>> beans = bm.getBeans(RuntimeManagedBean.class, new AnnotationLiteral[0]);//don't use any, must be none
		if (beans.size() > 0) {
			Bean<RuntimeManagedBean> bean = (Bean<RuntimeManagedBean>) beans.iterator().next();
			CreationalContext<RuntimeManagedBean> ctx = bm.createCreationalContext(bean);
			//passing qualifier information via thread locals to the CDI scope context is not ideal but given the thread specific nature of dependency injection it is acceptable
			try {
				managedInstanceId.set(id);
				return (RuntimeManagedBean) bm.getReference(bean, RuntimeManagedBean.class, ctx);
			} finally {
				managedInstanceId.remove();
			}
		}
		return null;

	}
}
