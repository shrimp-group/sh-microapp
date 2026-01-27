package com.wkclz.micro.fun.engine.impl;

import com.wkclz.micro.fun.engine.ScriptEngine;
import com.wkclz.micro.fun.pojo.entity.FunFunction;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * @author shrimp
 */
public class GroovyEngineImpl extends ScriptEngine {

    private Script script;

    public GroovyEngineImpl(FunFunction fun) {
        super(fun);
        initContext();
    }

    @Override
    public Object exec(String param) {
        Script script = this.script;
        Binding binding = new Binding();
        binding.setVariable(this.getFunParams(), param);
        script.setBinding(binding);
        return script.run();
    }

    private void initContext() {
        String template = """
            %s
            """;

        String body = this.getFunBody();
        this.setScript(String.format(template, body));

        GroovyShell shell = new GroovyShell();
        this.script = shell.parse(this.getScript());
    }

    private static FunFunction getDemo() {
        // demo function
        String groovy = """
            return 'Hello, ' + name + '!'
            """;
        FunFunction fun = new FunFunction();
        fun.setFunLanguage("Groovy");
        fun.setFunCode("sayHello");
        fun.setFunParams("name");
        fun.setFunBody("return 'Hello, ' + name + '!';");
        fun.setFunReturn(String.class.getSimpleName());
        return fun;
    }


    public static void main(String[] args) {
        FunFunction fun = getDemo();
        GroovyEngineImpl engine = new GroovyEngineImpl(fun);
        Object value = engine.exec("GraalVM, I'm Groovy");
        System.out.println(value);
    }

}