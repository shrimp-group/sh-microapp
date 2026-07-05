package com.wkclz.micro.fun.engine.impl;

import org.graalvm.polyglot.Context;


/**
 * @author shrimp
 */
public class GraalvmLanguage {

    public static void main(String[] args) {
        // 获取所有可用的语言
        try (Context context = Context.newBuilder().allowAllAccess(true).build()) {
            for (String language : context.getEngine().getLanguages().keySet()) {
                System.out.println(language);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
