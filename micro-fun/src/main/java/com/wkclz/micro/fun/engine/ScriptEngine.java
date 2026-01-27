package com.wkclz.micro.fun.engine;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.fun.pojo.entity.FunFunction;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author shrimp
 */
@Data
public abstract class ScriptEngine {

    private String funLanguage;
    private String funCode;
    private String funParams;
    private String funBody;
    private Class<?> funReturn;

    // 最终的脚本
    private String script;

    public ScriptEngine(FunFunction fun) {
        this.funLanguage = fun.getFunLanguage();
        this.funCode = fun.getFunCode();
        this.funParams = fun.getFunParams();
        this.funBody = fun.getFunBody();

        String rt = fun.getFunReturn();
        if ("String".equals(rt)) {
            this.funReturn = String.class;
        }
        if ("Integer".equals(rt)) {
            this.funReturn = Integer.class;
        }
        if ("Long".equals(rt)) {
            this.funReturn = Long.class;
        }
        if ("Double".equals(rt)) {
            this.funReturn = Double.class;
        }
        if ("BigDecimal".equals(rt)) {
            this.funReturn = BigDecimal.class;
        }
    }

    public Object exec(String param) {
        throw ValidationException.of("请使用实现");
    }

}
