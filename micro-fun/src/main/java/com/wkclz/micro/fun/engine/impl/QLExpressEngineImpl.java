package com.wkclz.micro.fun.engine.impl;

import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.IExpressContext;
import com.wkclz.micro.fun.engine.ScriptEngine;
import com.wkclz.micro.fun.pojo.entity.FunFunction;

/**
 * @author shrimp
 */
public class QLExpressEngineImpl extends ScriptEngine {

    private ExpressRunner runner;

    public QLExpressEngineImpl(FunFunction fun) {
        super(fun);
        initContext();
    }

    @Override
    public Object exec(String param) {
        IExpressContext<String, Object> context = new DefaultContext<>();
        context.put("param", param);
        try {
            return runner.execute(this.getFunBody(), context, null, true, false);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute QLExpress script", e);
        }
    }

    private void initContext() {
        String template = """
            %s
            """;
        this.runner = new ExpressRunner();
    }


    private static FunFunction getDemo() {
        String ql = """
            param + " World!"
            """;

        FunFunction fun = new FunFunction();
        fun.setFunBody(ql);
        fun.setFunReturn(String.class.getSimpleName());
        return fun;
    }


    public static void main(String[] args) {
        FunFunction fun = getDemo();
        QLExpressEngineImpl engine = new QLExpressEngineImpl(fun);
        Object value = engine.exec("GraalVM, I'm QLExpress");
        System.out.println(value);
    }

}