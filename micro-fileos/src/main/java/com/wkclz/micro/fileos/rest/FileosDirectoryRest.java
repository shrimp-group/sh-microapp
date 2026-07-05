package com.wkclz.micro.fileos.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.fileos.bean.entity.MdmFileosDirectory;
import com.wkclz.micro.fileos.bean.req.DirectoryInfoReq;
import com.wkclz.micro.fileos.bean.req.DirectoryListReq;
import com.wkclz.micro.fileos.bean.req.DirectoryTreeReq;
import com.wkclz.micro.fileos.bean.resp.DirectoryResp;
import com.wkclz.micro.fileos.bean.resp.DirectoryTreeResp;
import com.wkclz.micro.fileos.service.MdmFileosDirectoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.wkclz.tool.utils.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(Route.PREFIX)
@Tag(name = "目录管理")
@Validated
public class FileosDirectoryRest {

    @Autowired
    private MdmFileosDirectoryService mdmFileosDirectoryService;

    @GetMapping(Route.DIRECTORY_LIST)
    @Operation(summary = "目录列表")
    public R<List<DirectoryResp>> list(@Valid DirectoryListReq req) {
        List<MdmFileosDirectory> list = mdmFileosDirectoryService.getDirectoryList(req.getParentPath(), req.getBucketName(), null);
        List<DirectoryResp> cp = BeanUtil.cp(list, DirectoryResp.class);
        return R.ok(cp);
    }

    @GetMapping(Route.DIRECTORY_TREE)
    @Operation(summary = "目录树")
    public R<List<DirectoryTreeResp>> tree(@Valid DirectoryTreeReq req) {
        List<MdmFileosDirectory> directories = mdmFileosDirectoryService.getDirectoryTree(req.getBucketName(), null);
        List<DirectoryTreeResp> tree = buildTree(directories);
        return R.ok(tree);
    }

    @GetMapping(Route.DIRECTORY_INFO)
    @Operation(summary = "目录详情")
    public R<DirectoryResp> info(@Valid DirectoryInfoReq req) {
        MdmFileosDirectory directory = mdmFileosDirectoryService.getDirectoryByPath(req.getDirPath(), req.getBucketName(), null);
        DirectoryResp resp = BeanUtil.cp(directory, DirectoryResp.class);
        return R.ok(resp);
    }


    private List<DirectoryTreeResp> buildTree(List<MdmFileosDirectory> directories) {
        Map<String, DirectoryTreeResp> map = new LinkedHashMap<>();
        for (MdmFileosDirectory dir : directories) {
            DirectoryTreeResp resp = BeanUtil.cp(dir, DirectoryTreeResp.class);
            resp.setChildren(new ArrayList<>());
            map.put(dir.getDirPath(), resp);
        }
        List<DirectoryTreeResp> roots = new ArrayList<>();
        for (MdmFileosDirectory dir : directories) {
            DirectoryTreeResp resp = map.get(dir.getDirPath());
            if (dir.getParentPath() == null || !map.containsKey(dir.getParentPath())) {
                roots.add(resp);
            } else {
                DirectoryTreeResp parent = map.get(dir.getParentPath());
                parent.getChildren().add(resp);
            }
        }
        return roots;
    }
}
