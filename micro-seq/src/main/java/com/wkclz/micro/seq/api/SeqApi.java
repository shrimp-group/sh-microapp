package com.wkclz.micro.seq.api;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.seq.service.MdmSequenceService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SeqApi {

    @Autowired
    private MdmSequenceService mdmSequenceService;

    public String genSequence(String prefix) {
        return genSequence(prefix, 4, null);
    }

    public String genSequence(String prefix, String seqName) {
        return genSequence(prefix, 4, seqName);
    }
    public String genSequence(String prefix, Integer length, String seqName) {
        List<String> seqs = genSequences(prefix, 1, length, seqName);
        if (CollectionUtils.isEmpty(seqs)) {
            throw ValidationException.of("生成失败，没有可返回的序列!");
        }
        return seqs.get(0);
    }
    public List<String> genSequences(String prefix, Integer size, String seqName) {
        return genSequences(prefix, size, 4, seqName);
    }
    public synchronized List<String> genSequences(String prefix, Integer size, Integer length, String seqName) {
        List<String> seqs = mdmSequenceService.genSequences(prefix, size, length, seqName);
        return seqs.stream().map(t -> prefix + t).collect(Collectors.toList());
    }

}
