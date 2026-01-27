package com.wkclz.micro.fun.engine.impl;

import com.wkclz.micro.fun.engine.ScriptEngine;
import com.wkclz.micro.fun.pojo.entity.FunFunction;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

/**
 * @author shrimp
 */
public class RubyEngineImpl extends ScriptEngine {

    private Context context;

    public RubyEngineImpl(FunFunction fun) {
        super(fun);
        initContext();
    }

    @Override
    public Object exec(String param) {
        Value function = this.context.getBindings("ruby").getMember(this.getFunCode());
        Value result = function.execute(param);
        return result.as(this.getFunReturn());
    }

    private void initContext() {
        String template = """
            def %s(%s)
              %s
            end
            """;
        String name = this.getFunCode();
        String params = this.getFunParams();
        String body = this.getFunBody();

        if (params == null) {
            params = "";
        }

        this.setScript(String.format(template, name, params, body));
        Context context = Context.create();
        context.eval("ruby", this.getScript());
        this.context = context;
    }

    private static FunFunction getDemo() {
        // demo function
        String ruby = """
            def sayHello(name)
              "Hello, " + name + "!"
            end
            """;

        FunFunction fun = new FunFunction();
        fun.setFunLanguage("Ruby");
        fun.setFunCode("sayHello");
        fun.setFunParams("name");
        fun.setFunBody("return 'Hello, ' + name + '!';");
        fun.setFunReturn(String.class.getSimpleName());
        return fun;
    }

    public static void main(String[] args) {
        FunFunction fun = getDemo();
        RubyEngineImpl engine = new RubyEngineImpl(fun);
        Object value = engine.exec("GraalVM, I'm Ruby");
        System.out.println(value);
    }

}
