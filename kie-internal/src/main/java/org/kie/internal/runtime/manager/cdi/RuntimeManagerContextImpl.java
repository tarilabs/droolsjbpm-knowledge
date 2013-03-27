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

import java.lang.annotation.Annotation;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import org.kie.internal.runtime.manager.cdi.RuntimeManagerContextState.ScopedInstance;
import org.kie.internal.runtime.manager.cdi.RuntimeManagerContextState.ThreadLocalState;

/**
 *
 * @author salaboy
 */
public class RuntimeManagerContextImpl implements Context {

    public RuntimeManagerContextImpl() {
        System.out.println(" >>>> New Runtime Manager Context Created!");
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return RuntimeManagerScoped.class;
    }

    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
       Bean bean = (Bean) contextual;
		ScopedInstance si = null;
		ThreadLocalState tscope = RuntimeManagerContextState.state.get();
		if (RuntimeManagedBean.class.isAssignableFrom(bean.getBeanClass())) {
			//Check if qualifier is present
			String id = RuntimeManagedBeanProducer.managedInstanceId.get();
			if (id == null) {//no scope present, get scope singleton
				id = "";
			}
			si = tscope.fooInstances.get(id);
			if (si == null) {
				si = new ScopedInstance();
				si.bean = bean;
				si.ctx = creationalContext;
				si.instance = bean.create(creationalContext);
				tscope.fooInstances.put(id, si);
				tscope.allInstances.add(si);
			}

			return (T) si.instance;
		} else {
			si = new ScopedInstance();
			si.bean = bean;
			si.ctx = creationalContext;
			si.instance = bean.create(creationalContext);
			tscope.allInstances.add(si);
		}
		return (T)si.instance;

    }

    @Override
    public <T> T get(Contextual<T> contextual) {
        throw new UnsupportedOperationException("Not Supported Yet");
    }

    @Override
    public boolean isActive() {
        return RuntimeManagerContextState.state.get() != null ? true : false;
    }
}
