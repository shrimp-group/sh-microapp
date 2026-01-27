package com.wkclz.micro.fun.engine.impl;

import com.wkclz.micro.fun.engine.ScriptEngine;
import com.wkclz.micro.fun.pojo.entity.FunFunction;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

/**
 * @author shrimp
 */
public class JavaScriptEngineImpl extends ScriptEngine {

    private Context context;

    public JavaScriptEngineImpl(FunFunction fun) {
        super(fun);
        initContext();
    }

    @Override
    public Object exec(String param) {
        Value function = this.context.getBindings("js").getMember(this.getFunCode());
        Value result = function.execute(param);
        return result.as(this.getFunReturn());
    }

    private void initContext() {
        String template = """
            function %s(%s) {
              %s
            }
            """;

        String name = this.getFunCode();
        String params = this.getFunParams();
        String body = this.getFunBody();

        if (params == null) {
            params = "";
        }

        this.setScript(String.format(template, name, params, body));
        Context context = Context.create();
        context.eval("js", this.getScript());
        this.context = context;
    }


    private static FunFunction getDemo() {
        // demo function
        String js = """
            function sayHello(param) {
                return 'Hello, ' + name + '!';
            }
            """;

        FunFunction fun = new FunFunction();
        fun.setFunLanguage("JavaScript");
        fun.setFunCode("sayHello");
        fun.setFunParams("name");
        fun.setFunBody("return 'Hello, ' + name + '!';");
        fun.setFunReturn(String.class.getSimpleName());
        return fun;
    }

    public static void main(String[] args) {
        FunFunction fun = getDemo();
        JavaScriptEngineImpl engine = new JavaScriptEngineImpl(fun);
        Object value = engine.exec("GraalVM, I'm JavaScript");
        System.out.println(value);
    }


}
