/*
 * Copyright (c) 2015, Kasra Faghihi, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.offbynull.coroutines.instrumenter;

import com.offbynull.coroutines.user.Continuation;
import com.offbynull.coroutines.user.LockState;
import com.offbynull.coroutines.user.MethodState;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

abstract class ContinuationPoint {

    protected static final Method CONTINUATION_GETMODE_METHOD
            = MethodUtils.getAccessibleMethod(Continuation.class, "getMode");
    protected static final Method CONTINUATION_SETMODE_METHOD
            = MethodUtils.getAccessibleMethod(Continuation.class, "setMode", Integer.TYPE);
    protected static final Method CONTINUATION_GETPENDINGSIZE_METHOD
            = MethodUtils.getAccessibleMethod(Continuation.class, "getPendingSize");
    protected static final Method CONTINUATION_CLEAREXCESSPENDING_METHOD
            = MethodUtils.getAccessibleMethod(Continuation.class, "clearExcessPending", Integer.TYPE);
    protected static final Method CONTINUATION_ADDPENDING_METHOD
            = MethodUtils.getAccessibleMethod(Continuation.class, "addPending", MethodState.class);
    protected static final Method CONTINUATION_REMOVELASTPENDING_METHOD
            = MethodUtils.getAccessibleMethod(Continuation.class, "removeLastPending");
    protected static final Method CONTINUATION_REMOVEFIRSTSAVED_METHOD
            = MethodUtils.getAccessibleMethod(Continuation.class, "removeFirstSaved");
    protected static final Constructor<MethodState> METHODSTATE_INIT_METHOD
            = ConstructorUtils.getAccessibleConstructor(MethodState.class, Integer.TYPE, Object[].class, Object[].class, LockState.class);
    protected static final Method METHODSTATE_GETCONTINUATIONPOINT_METHOD
            = MethodUtils.getAccessibleMethod(MethodState.class, "getContinuationPoint");
    protected static final Method METHODSTATE_GETLOCALTABLE_METHOD
            = MethodUtils.getAccessibleMethod(MethodState.class, "getLocalTable");
    protected static final Method METHODSTATE_GETSTACK_METHOD
            = MethodUtils.getAccessibleMethod(MethodState.class, "getStack");
    
    private final int id;
    private final AbstractInsnNode invokeInsnNode;
    private final Integer lineNumber;
    private final Frame<BasicValue> frame;
    private final Type returnType;
    private final FlowInstrumentationVariables flowInstrumentationVariables;
    private final MonitorInstrumentationInstructions monitorInstrumentationInstructions;

    ContinuationPoint(int id, AbstractInsnNode invokeInsnNode, LineNumberNode invokeLineNumberNode, Frame<BasicValue> frame,
            Type returnType,
            FlowInstrumentationVariables flowInstrumentationVariables,
            MonitorInstrumentationInstructions monitorInstrumentationInstructions) {
        Validate.notNull(invokeInsnNode);
        Validate.notNull(frame);
        Validate.notNull(flowInstrumentationVariables);
        Validate.notNull(monitorInstrumentationInstructions);
        
        this.id = id;
        this.invokeInsnNode = invokeInsnNode;
        this.lineNumber = invokeLineNumberNode != null ? invokeLineNumberNode.line : null;
        this.frame = frame;
        this.returnType = returnType;
        this.flowInstrumentationVariables = flowInstrumentationVariables;
        this.monitorInstrumentationInstructions = monitorInstrumentationInstructions;
    }
    
    final int getId() {
        return id;
    }

    final AbstractInsnNode getInvokeInsnNode() {
        return invokeInsnNode;
    }

    final Integer getLineNumber() {
        return lineNumber;
    }

    final Frame<BasicValue> getFrame() {
        return frame;
    }

    final Type getReturnType() {
        return returnType;
    }

    final FlowInstrumentationVariables getFlowInstrumentationVariables() {
        return flowInstrumentationVariables;
    }

    final MonitorInstrumentationInstructions getMonitorInstrumentationInstructions() {
        return monitorInstrumentationInstructions;
    }
    
    // the logic that gets executed when the switch statement at the top of the method determines that the method is being loaded
    abstract InsnList generateLoadInstructions();
    
    // the logic that replaces invocation
    abstract InsnList generateInvokeReplacementInstructions();
}
