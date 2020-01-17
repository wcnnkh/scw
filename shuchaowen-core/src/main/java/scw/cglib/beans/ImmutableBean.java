/*
 * Copyright 2003,2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package scw.cglib.beans;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

import scw.asm.ClassVisitor;
import scw.asm.Type;
import scw.cglib.core.AbstractClassGenerator;
import scw.cglib.core.ClassEmitter;
import scw.cglib.core.CodeEmitter;
import scw.cglib.core.CGLIBConstants;
import scw.cglib.core.EmitUtils;
import scw.cglib.core.MethodInfo;
import scw.cglib.core.ReflectUtils;
import scw.cglib.core.Signature;
import scw.cglib.core.CGLIBTypeUtils;
/**
 * @author Chris Nokleberg
 */

@SuppressWarnings({"rawtypes"})
public class ImmutableBean
{
    private static final Type ILLEGAL_STATE_EXCEPTION =
      CGLIBTypeUtils.parseType("IllegalStateException");
    private static final Signature CSTRUCT_OBJECT =
      CGLIBTypeUtils.parseConstructor("Object");
    private static final Class[] OBJECT_CLASSES = { Object.class };
    private static final String FIELD_NAME = "CGLIB$RWBean";

    private ImmutableBean() {
    }

    public static Object create(Object bean) {
        Generator gen = new Generator();
        gen.setBean(bean);
        return gen.create();
    }

    public static class Generator extends AbstractClassGenerator {
        private static final Source SOURCE = new Source(ImmutableBean.class.getName());
        private Object bean;
        private Class target;

        public Generator() {
            super(SOURCE);
        }

        public void setBean(Object bean) {
            this.bean = bean;
            target = bean.getClass();
        }

        protected ClassLoader getDefaultClassLoader() {
            return target.getClassLoader();
        }

        protected ProtectionDomain getProtectionDomain() {
        	return ReflectUtils.getProtectionDomain(target);
        }

        public Object create() {
            String name = target.getName();
            setNamePrefix(name);
            return super.create(name);
        }

        public void generateClass(ClassVisitor v) {
            Type targetType = Type.getType(target);
            ClassEmitter ce = new ClassEmitter(v);
            ce.begin_class(CGLIBConstants.V1_2,
                           CGLIBConstants.ACC_PUBLIC,
                           getClassName(),
                           targetType,
                           null,
                           CGLIBConstants.SOURCE_FILE);

            ce.declare_field(CGLIBConstants.ACC_FINAL | CGLIBConstants.ACC_PRIVATE, FIELD_NAME, targetType, null);

            CodeEmitter e = ce.begin_method(CGLIBConstants.ACC_PUBLIC, CSTRUCT_OBJECT, null);
            e.load_this();
            e.super_invoke_constructor();
            e.load_this();
            e.load_arg(0);
            e.checkcast(targetType);
            e.putfield(FIELD_NAME);
            e.return_value();
            e.end_method();

            PropertyDescriptor[] descriptors = ReflectUtils.getBeanProperties(target);
            Method[] getters = ReflectUtils.getPropertyMethods(descriptors, true, false);
            Method[] setters = ReflectUtils.getPropertyMethods(descriptors, false, true);

            for (int i = 0; i < getters.length; i++) {
                MethodInfo getter = ReflectUtils.getMethodInfo(getters[i]);
                e = EmitUtils.begin_method(ce, getter, CGLIBConstants.ACC_PUBLIC);
                e.load_this();
                e.getfield(FIELD_NAME);
                e.invoke(getter);
                e.return_value();
                e.end_method();
            }

            for (int i = 0; i < setters.length; i++) {
                MethodInfo setter = ReflectUtils.getMethodInfo(setters[i]);
                e = EmitUtils.begin_method(ce, setter, CGLIBConstants.ACC_PUBLIC);
                e.throw_exception(ILLEGAL_STATE_EXCEPTION, "Bean is immutable");
                e.end_method();
            }

            ce.end_class();
        }

        protected Object firstInstance(Class type) {
            return ReflectUtils.newInstance(type, OBJECT_CLASSES, new Object[]{ bean });
        }

        // TODO: optimize
        protected Object nextInstance(Object instance) {
            return firstInstance(instance.getClass());
        }
    }
}
