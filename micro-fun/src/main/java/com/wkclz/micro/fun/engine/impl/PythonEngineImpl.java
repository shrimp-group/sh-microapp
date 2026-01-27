package com.wkclz.micro.fun.engine.impl;

import com.wkclz.micro.fun.engine.ScriptEngine;
import com.wkclz.micro.fun.pojo.entity.FunFunction;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

public class PythonEngineImpl extends ScriptEngine {

    private Context context;

    public PythonEngineImpl(FunFunction fun) {
        super(fun);
        initContext();
    }

    @Override
    public Object exec(String param) {
        Value function = this.context.getBindings("python").getMember(this.getFunCode());
        Value result = function.execute(param);
        return result.as(this.getFunReturn());
    }

    private void initContext() {
        String template = """
            def %s(%s):
                %s
            """;

        String name = this.getFunCode();
        String params = this.getFunParams();
        String body = this.getFunBody();

        if (params == null) {
            params = "";
        }

        this.setScript(String.format(template, name, params, body));
        Context context = Context.create();
        context.eval("python", this.getScript());
        this.context = context;
    }

    private static FunFunction getDemo() {
        // demo function
        String python = """
            def sayHello(name):
                return "Hello, " + name + "!"
            """;

        FunFunction fun = new FunFunction();
        fun.setFunLanguage("Python");
        fun.setFunCode("sayHello");
        fun.setFunParams("name");
        fun.setFunBody("return 'Hello, ' + name + '!';");
        fun.setFunReturn(String.class.getSimpleName());
        return fun;
    }

    public static void main(String[] args) {
        FunFunction fun = getDemo();
        PythonEngineImpl engine = new PythonEngineImpl(fun);
        Object value = engine.exec("GraalVM, I'm Python");
        System.out.println(value);
    }

}