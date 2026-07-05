package com.wkclz.micro.dbview.bean.enums;

import java.util.Set;

public enum PermissionLevel {
    READ_ONLY(Set.of(
        SqlType.SELECT, SqlType.SHOW, SqlType.DESC, SqlType.EXPLAIN
    )),
    READ_WRITE(Set.of(
        SqlType.SELECT, SqlType.SHOW, SqlType.DESC, SqlType.EXPLAIN,
        SqlType.INSERT, SqlType.UPDATE, SqlType.DELETE
    )),
    DDL(Set.of(
        SqlType.SELECT, SqlType.SHOW, SqlType.DESC, SqlType.EXPLAIN,
        SqlType.INSERT, SqlType.UPDATE, SqlType.DELETE,
        SqlType.ALTER, SqlType.CREATE, SqlType.DROP, SqlType.RENAME
    ));

    private final Set<SqlType> allowedTypes;

    PermissionLevel(Set<SqlType> allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    public boolean isAllowed(SqlType sqlType) {
        return allowedTypes.contains(sqlType);
    }

    public static PermissionLevel of(String level) {
        if (level == null) {
            return null;
        }
        return switch (level.toUpperCase()) {
            case "READ_ONLY" -> READ_ONLY;
            case "READ_WRITE" -> READ_WRITE;
            case "DDL" -> DDL;
            default -> null;
        };
    }
}
