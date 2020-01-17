/*
 * Copyright 2003 The Apache Software Foundation
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
package scw.cglib.transform.impl;

import scw.asm.Type;
import scw.cglib.core.CodeEmitter;
import scw.cglib.core.CGLIBConstants;
import scw.cglib.core.Signature;
import scw.cglib.core.CGLIBTypeUtils;
import scw.cglib.transform.ClassEmitterTransformer;

public class AccessFieldTransformer extends ClassEmitterTransformer {
    private Callback callback;

    public AccessFieldTransformer(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        String getPropertyName(Type owner, String fieldName);
    }

    public void declare_field(int access, final String name, Type type, Object value) {
        super.declare_field(access, name, type, value);

        String property = CGLIBTypeUtils.upperFirst(callback.getPropertyName(getClassType(), name));
        if (property != null) {
            CodeEmitter e;
            e = begin_method(CGLIBConstants.ACC_PUBLIC,
                             new Signature("get" + property,
                                           type,
                                           CGLIBConstants.TYPES_EMPTY),
                             null);
            e.load_this();
            e.getfield(name);
            e.return_value();
            e.end_method();

            e = begin_method(CGLIBConstants.ACC_PUBLIC,
                             new Signature("set" + property,
                                           Type.VOID_TYPE,
                                           new Type[]{ type }),
                             null);
            e.load_this();
            e.load_arg(0);
            e.putfield(name);
            e.return_value();
            e.end_method();
        }
    }
}
