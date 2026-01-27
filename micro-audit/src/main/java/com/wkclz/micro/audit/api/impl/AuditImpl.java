package com.wkclz.micro.audit.api.impl;

import com.alibaba.fastjson2.JSONObject;
import com.wkclz.core.base.BaseEntity;
import com.wkclz.core.base.PageData;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.audit.api.AuditApi;
import com.wkclz.micro.audit.pojo.dto.ChangeLog;
import com.wkclz.micro.audit.pojo.dto.ChangeLogItem;
import com.wkclz.micro.audit.pojo.entity.MdmChangeLog;
import com.wkclz.micro.audit.service.MdmChangeLogService;
import com.wkclz.micro.audit.utils.AuditCompareUtil;
import com.wkclz.mybatis.bean.ColumnQuery;
import com.wkclz.mybatis.service.TableInfoService;
import com.wkclz.redis.helper.RedisIdGenerator;
import com.wkclz.tool.bean.JavaField;
import com.wkclz.tool.utils.BeanUtil;
import com.wkclz.tool.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AuditImpl implements AuditApi {

    @Autowired
    private TableInfoService tableInfoService;
    @Autowired
    private RedisIdGenerator redisIdGenerator;
    @Autowired
    private MdmChangeLogService mdmChangeLogService;

    @Override
    public String getBatchNo() {
        return redisIdGenerator.generateIdWithPrefix("audit_");
    }

    @Override
    public <F extends BaseEntity> void create(F to) {
        if (to == null) {
            throw ValidationException.of("audit create, from can not be null!");
        }
        create(Collections.singletonList(to), null);
    }

    @Override
    public <F extends BaseEntity> void create(List<F> tos) {
        if (CollectionUtils.isEmpty(tos)) {
            throw ValidationException.of("audit create, from can not be empty!");
        }
        create(tos, null);
    }

    @Override
    public <F extends BaseEntity> void create(F to, String batchNo) {
        if (to == null) {
            throw ValidationException.of("audit create, to can not be null!");
        }
        create(Collections.singletonList(to), batchNo);
    }

    @Override
    public <F extends BaseEntity> void create(List<F> tos, String batchNo) {
        if (CollectionUtils.isEmpty(tos)) {
            throw ValidationException.of("audit create, to can not be empty!");
        }
        if (StringUtils.isBlank(batchNo)) {
            batchNo = getBatchNo();
        }
        List<MdmChangeLog> logs = new ArrayList<>();
        String tableName = AuditCompareUtil.getTableName(tos.get(0));
        for (F to : tos) {
            to.setVersion(to.getVersion() == null ? 0 : to.getVersion() + 1);
            MdmChangeLog log = new MdmChangeLog();
            logs.add(log);
            log.setBatchNo(batchNo);
            log.setDataId(to.getId());
            log.setDataVersion(0);
            log.setTableName(tableName);
            log.setOperateType("INSERT");
            log.setDataTo(AuditCompareUtil.getDataValueJson(to));
        }
        mdmChangeLogService.insertBatch(logs);
    }

    @Override
    public <F extends BaseEntity, T extends BaseEntity> void modify(F from, T to) {
        if (from == null || to == null) {
            throw ValidationException.of("audit modify, from or to can not be null!");
        }
        modify(Collections.singletonList(from), Collections.singletonList(to), null);
    }

    @Override
    public <F extends BaseEntity, T extends BaseEntity> void modify(List<F> froms, List<T> tos) {
        if (CollectionUtils.isEmpty(froms) || CollectionUtils.isEmpty(tos)) {
            throw ValidationException.of("audit modify, from or to can not be null!");
        }
        modify(froms, tos, null);
    }

    @Override
    public <F extends BaseEntity, T extends BaseEntity> void modify(F from, T to, String batchNo) {
        if (from == null || to == null) {
            throw ValidationException.of("audit modify, from or to can not be null!");
        }
        modify(Collections.singletonList(from), Collections.singletonList(to), batchNo);
    }

    @Override
    public <F extends BaseEntity, T extends BaseEntity> void modify(List<F> froms, List<T> tos, String batchNo) {
        if (CollectionUtils.isEmpty(froms) || CollectionUtils.isEmpty(tos)) {
            throw ValidationException.of("audit modify, from or to can not be null!");
        }
        if (froms.size() != tos.size()) {
            throw ValidationException.of("audit modify, from count must be the same with to count!");
        }
        if (StringUtils.isBlank(batchNo)) {
            batchNo = getBatchNo();
        }

        List<MdmChangeLog> logs = new ArrayList<>();
        String tableName = AuditCompareUtil.getTableName(froms.get(0));
        for (int i = 0; i < froms.size(); i++) {
            F from = froms.get(i);
            T to = tos.get(i);
            if (from.getId() == null) {
                throw ValidationException.of("modify, from can not without id!");
            }
            if (to.getId() == null) {
                throw ValidationException.of("modify, to can not without id!");
            }
            if (!from.getId().equals(to.getId())) {
                throw ValidationException.of("modify, [from] id is not equals to [to] id!");
            }
            to.setVersion(to.getVersion() == null ? 0 : to.getVersion() + 1);
            MdmChangeLog log = new MdmChangeLog();
            logs.add(log);
            log.setBatchNo(batchNo);
            log.setDataId(from.getId());
            log.setDataVersion(from.getVersion() + 1);
            log.setTableName(tableName);
            log.setOperateType("UPDATE");
            log.setDataFrom(AuditCompareUtil.getDataValueJson(from));
            log.setDataTo(AuditCompareUtil.getDataValueJson(to));
        }
        mdmChangeLogService.insertBatch(logs);
    }

    @Override
    public <F extends BaseEntity> void delete(F from) {
        if (from == null) {
            throw ValidationException.of("audit delete, from can not be null!");
        }
        delete(Collections.singletonList(from), null);
    }

    @Override
    public <F extends BaseEntity> void delete(List<F> froms) {
        if (CollectionUtils.isEmpty(froms)) {
            throw ValidationException.of("audit delete, from can not be empty!");
        }
        delete(froms, null);
    }

    @Override
    public <F extends BaseEntity> void delete(F from, String batchNo) {
        if (from == null) {
            throw ValidationException.of("audit delete, from can not be null!");
        }
        delete(Collections.singletonList(from), batchNo);
    }

    @Override
    public <F extends BaseEntity> void delete(List<F> froms, String batchNo) {
        if (CollectionUtils.isEmpty(froms)) {
            throw ValidationException.of("audit delete, from can not be empty!");
        }
        if (StringUtils.isBlank(batchNo)) {
            batchNo = getBatchNo();
        }

        List<MdmChangeLog> logs = new ArrayList<>();
        String tableName = AuditCompareUtil.getTableName(froms.get(0));
        for (F from : froms) {
            MdmChangeLog log = new MdmChangeLog();
            logs.add(log);
            log.setBatchNo(batchNo);
            log.setDataId(from.getId());
            log.setDataVersion(from.getVersion() + 1);
            log.setTableName(tableName);
            log.setOperateType("DELETE");
            log.setDataFrom(AuditCompareUtil.getDataValueJson(from));
        }
        mdmChangeLogService.insertBatch(logs);
    }

    @Override
    public <T extends BaseEntity> PageData<ChangeLog> getLogPage(ChangeLog<T> dto) {
        String tableName = AuditCompareUtil.getTableName(dto.getClazz());
        MdmChangeLog param = new MdmChangeLog();
        param.setTableName(tableName);
        param.setBatchNo(dto.getBatchNo());
        param.setDataId(dto.getDataId());
        param.setOperateType(dto.getOperateType());
        param.setCreateBy(dto.getCreateBy());
        param.setTimeFrom(dto.getTimeFrom());
        param.setTimeTo(dto.getTimeTo());
        param.setKeyword(dto.getKeyword());

        PageData<MdmChangeLog> page = mdmChangeLogService.getChangeLogPage(param);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return PageData.of(Collections.emptyList(), page.getTotal(), page.getCurrent(), page.getSize());
        }

        // 获取列名
        ColumnQuery query = new ColumnQuery();
        query.setTableName(tableName);
        List<ColumnQuery> infos = tableInfoService.getColumnInfos4Options(query);
        for (ColumnQuery info : infos) {
            info.setColumnName(StringUtil.underlineToCamel(info.getColumnName()));
        }

        Map<String, JavaField> getters = BeanUtil.getJavaField(dto.getClazz());
        List<ChangeLog> dtos = page.getRecords().stream().map(ChangeLog::copy).collect(Collectors.toList());

        for (ChangeLog d : dtos) {
            List<ChangeLogItem> items = new ArrayList<>();
            d.setItems(items);

            // 数据转换成实体
            T from = null;
            T to = null;
            if (d.getDataFrom() != null) {
                from = JSONObject.parseObject(d.getDataFrom(), dto.getClazz());
                d.setDataFrom(null);
            }
            if (d.getDataTo() != null) {
                to = JSONObject.parseObject(d.getDataTo(), dto.getClazz());
                d.setDataTo(null);
            }

            for (ColumnQuery info : infos) {
                JavaField field = getters.get(info.getColumnName());
                if (field == null) {
                    continue;
                }
                Method getter = field.getGetter();
                if (getter == null) {
                    continue;
                }
                try {
                    // 旧值
                    Object o = from == null ? null : getter.invoke(from);
                    // 新值
                    Object n = to == null ? null : getter.invoke(to);

                    // 都为空，一定相同
                    if (o == null && n == null) {
                        continue;
                    }

                    // 如果旧值是 null，则新值一定不是 null
                    if (o == null) {
                        ChangeLogItem item = new ChangeLogItem();
                        item.setOldValue(o);
                        item.setNewValue(n);
                        item.setColumnName(info.getColumnName());
                        item.setColumnDesc(info.getColumnComment());
                        items.add(item);
                    }

                    // 如果旧值不是 null，则与新值对比
                    if (o != null && !o.equals(n)) {
                        ChangeLogItem item = new ChangeLogItem();
                        item.setOldValue(o);
                        item.setNewValue(n);
                        item.setColumnName(info.getColumnName());
                        item.setColumnDesc(info.getColumnComment());
                        items.add(item);
                    }
                } catch (InvocationTargetException | IllegalAccessException e) {
                    // 报错了不处理，继续前行
                    log.error("can not invoke {}", field.getGetter().getName());
                } catch (Exception ex) {
                    // 报错了不处理，继续前行
                    log.error("can not invoke {}", ex.getMessage());
                }
            }
        }
        return PageData.of(dtos, page.getTotal(), page.getCurrent(), page.getSize());
    }
}
