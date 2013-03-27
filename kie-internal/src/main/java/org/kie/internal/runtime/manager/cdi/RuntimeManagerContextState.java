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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

/**
 *
 * @author salaboy
 */
public class RuntimeManagerContextState {

    ThreadLocalState scope = null;
    public static ThreadLocal<ThreadLocalState> state = new ThreadLocal<ThreadLocalState>();

    public static class ThreadLocalState {
        //These should be converted to thread safe collections

        Set<ScopedInstance<RuntimeManagedBean>> allInstances = new HashSet<ScopedInstance<RuntimeManagedBean>>();
        Map<String, ScopedInstance<RuntimeManagedBean>> fooInstances = new HashMap<String, ScopedInstance<RuntimeManagedBean>>();
    }
    @Inject
    BeanManager bm;

    public void create() {
        scope = new ThreadLocalState();
    }

    public void begin() {
        if (state.get() != null) {
            throw new IllegalAccessError("Already in FooScope");
        }
        state.set(scope);
    }

    public void end() {
        if (state.get() == null) {
            throw new IllegalAccessError("Not in FooScope");
        }
        state.remove();
    }

    public <T> T newInstance(Class<T> clazz) {
        Set<Bean<?>> beans = bm.getBeans(clazz, new AnnotationLiteral<Any>() {
        });
        if (beans.size() > 0) {
            Bean bean = beans.iterator().next();
            CreationalContext cc = bm.createCreationalContext(bean);
            return (T) bm.getReference(bean, clazz, cc);
        }
        return null;
    }

    public void destroy() {
        //Since this is not a CDI NormalScope we are responsible for managing the entire lifecycle, including
        //destroying the beans
        for (ScopedInstance entry2 : scope.allInstances) {
            entry2.bean.destroy(entry2.instance, entry2.ctx);
        }
        scope = null;
    }

    public static class ScopedInstance<T> {

        Bean<T> bean;
        CreationalContext<T> ctx;
        T instance;
    }
}
