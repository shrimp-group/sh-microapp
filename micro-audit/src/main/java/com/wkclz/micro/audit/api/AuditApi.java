package com.wkclz.micro.audit.api;

import com.wkclz.core.base.BaseEntity;
import com.wkclz.core.base.PageData;
import com.wkclz.micro.audit.pojo.dto.ChangeLog;

import java.util.List;

public interface AuditApi {

    String getBatchNo();

    <F extends BaseEntity> void create(F from);
    <F extends BaseEntity> void create(List<F> froms);
    <F extends BaseEntity> void create(F from, String batchNo);
    <F extends BaseEntity> void create(List<F> froms, String batchNo);

    <F extends BaseEntity,T extends BaseEntity> void modify(F from, T to);
    <F extends BaseEntity,T extends BaseEntity> void modify(List<F> froms, List<T> tos);
    <F extends BaseEntity,T extends BaseEntity> void modify(F from, T to, String batchNo);
    <F extends BaseEntity,T extends BaseEntity> void modify(List<F> froms, List<T> tos, String batchNo);

    <F extends BaseEntity> void delete(F from);
    <F extends BaseEntity> void delete(List<F> froms);
    <F extends BaseEntity> void delete(F from, String batchNo);
    <F extends BaseEntity> void delete(List<F> froms, String batchNo);

    <T extends BaseEntity> PageData<ChangeLog> getLogPage(ChangeLog<T> dto);

}
