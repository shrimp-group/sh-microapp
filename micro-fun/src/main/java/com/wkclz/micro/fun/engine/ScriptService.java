package com.wkclz.micro.fun.engine;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.fun.engine.impl.*;
import com.wkclz.micro.fun.pojo.entity.FunFunction;
import com.wkclz.micro.fun.service.FunFunctionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ScriptService {

    private static final ConcurrentMap<String, ScriptEngine> FUN_CACHE = new ConcurrentHashMap<>();

    @Autowired
    private FunFunctionService funFunctionService;

    public ScriptEngine getEngine(String funCode) {
        if (StringUtils.isBlank(funCode)) {
            throw ValidationException.of("请指定函数名！");
        }
        ScriptEngine engine = FUN_CACHE.computeIfAbsent(funCode, s -> {
            FunFunction fun = funFunctionService.getFunction(funCode);
            if (fun == null) {
                fun = new FunFunction();
                fun.setFunCode(funCode);
                return new NonEngineImpl(fun);
            }
            return getScriptEngine(fun);
        });

        if (engine.getFunBody() == null) {
            throw ValidationException.of("不存在此函数: {}", funCode);
        }
        return engine;
    }

    public ScriptEngine getEngineTest(FunFunction fun) {
        if (fun == null || StringUtils.isBlank(fun.getFunCode())) {
            throw ValidationException.of("请指定函数名！");
        }
        return getScriptEngine(fun);

    }

    private static ScriptEngine getScriptEngine(FunFunction fun) {
        return switch (fun.getFunLanguage()) {
            case "JavaScript" -> new JavaScriptEngineImpl(fun);
            case "Python" -> new PythonEngineImpl(fun);
            case "Groovy" -> new GroovyEngineImpl(fun);
            case "QLExpress" -> new QLExpressEngineImpl(fun);
            case "Ruby" -> new RubyEngineImpl(fun);
            default -> throw ValidationException.of("暂不支持的语言: {}", fun.getFunLanguage());
        };
    }


}
