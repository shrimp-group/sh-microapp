package com.wkclz.micro.fun.engine.impl;

import com.wkclz.micro.fun.engine.ScriptEngine;
import com.wkclz.micro.fun.pojo.entity.FunFunction;
import org.graalvm.polyglot.Context;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NonEngineImpl extends ScriptEngine {

    private static final ConcurrentMap<String, Context> CONTEXT_CACHE = new ConcurrentHashMap<>();

    public NonEngineImpl(FunFunction fun) {
        super(fun);
    }

}
