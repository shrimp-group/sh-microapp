package com.wkclz.micro.fun.engine.impl;

import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;


/**
 * @author shrimp
 */
@Slf4j
public class GraalvmLanguage {

    public static void main(String[] args) {
        // 获取所有可用的语言
        try (Context context = Context.newBuilder().allowAllAccess(true).build()) {
            for (String language : context.getEngine().getLanguages().keySet()) {
                log.info("{}", language);
            }
        } catch (Exception e) {
            log.error("获取 GraalVM 可用语言失败", e);
        }
    }

}
