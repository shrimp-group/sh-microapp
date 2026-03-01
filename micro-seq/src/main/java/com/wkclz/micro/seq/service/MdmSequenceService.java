package com.wkclz.micro.seq.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.seq.dao.MdmSequenceMapper;
import com.wkclz.micro.seq.pojo.entity.MdmSequence;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_sequence (序列生成) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 mdm_sequence 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class MdmSequenceService extends BaseService<MdmSequence, MdmSequenceMapper> {

    public PageData<MdmSequence> getSequencePage(MdmSequence entity) {
        return PageQuery.page(entity, mapper::getSequenceList);
    }

    public MdmSequence create(MdmSequence entity) {
        duplicateCheck(entity);
        mapper.insert(entity);
        return entity;
    }

    public MdmSequence update(MdmSequence entity) {
        duplicateCheck(entity);
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getId(), ResultCode.UPDATE_NO_VERSION.getMessage());
        MdmSequence oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        MdmSequence.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

    private void duplicateCheck(MdmSequence entity) {
        // 唯一条件为空，直接通过
        // 唯一条件不为空，请设置唯一条件
        MdmSequence param = new MdmSequence();
        // 唯一条件
        param.setPrefix(entity.getPrefix());
        param = selectOneByEntity(param);
        if (param == null) {
            return;
        }
        if (param.getId().equals(entity.getId())) {
            return;
        }
        // 查到有值，为新增或 id 不一样场景，为数据重复
        throw ValidationException.of(ResultCode.RECORD_DUPLICATE);
    }






    /**
     * 此序列生成，序列唯一性非常重要，生成的数量极少，牺牲性能保功能
     */

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.SERIALIZABLE)
    public List<String> genSequences(String prefix, Integer size, Integer length) {
        return genSequences(prefix, size, length, null);
    }
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.SERIALIZABLE)
    public synchronized List<String> genSequences(String prefix, Integer size, Integer length, String seqName) {
        if (StringUtils.isBlank(prefix)) {
            throw ValidationException.of("prefix 不能为空");
        }
        prefix = prefix.trim();
        if (size == null || size < 0) {
            size = 1;
        }

        MdmSequence seq = mapper.getSequenceInfo(prefix);
        if (seq == null) {
            if (length == null || length < 4) {
                length = 4;
            }
            if (StringUtils.isBlank(seqName)) {
                seqName = prefix;
            }
            seq = new MdmSequence();
            seq.setSeqName(seqName);
            seq.setPrefix(prefix);
            seq.setSequence(0);
            seq.setCodeLength(length);
            mapper.insertSequenceInfo(seq);
            seq = mapper.getSequenceInfo(prefix);
        }
        length = seq.getCodeLength();

        Integer sequence = seq.getSequence();
        List<String> seqs = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            sequence = sequence + 1;
            String s = fitLength(sequence, length);
            seqs.add(s);
        }
        seq.setSequence(sequence);
        Integer i = mapper.updateSequenceInfo(seq);
        if (i < 1) {
            throw ValidationException.of("编码生成竞争失败，为防止重复，已放弃生成，请重新提交！");
        }
        return seqs;
    }



    private static String fitLength(Integer sequence, Integer length) {
        String str = sequence + "";
        int i = length - str.length();
        if (i < 1) {
            return str;
        }
        return "0".repeat(i) + str;
    }



}

