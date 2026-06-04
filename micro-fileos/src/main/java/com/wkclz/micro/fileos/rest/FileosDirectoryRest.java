package com.wkclz.micro.fileos.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.fileos.bean.dto.MdmFileosDirectoryDto;
import com.wkclz.micro.fileos.bean.entity.MdmFileosDirectory;
import com.wkclz.micro.fileos.service.MdmFileosDirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Route.PREFIX)
public class FileosDirectoryRest {

    @Autowired
    private MdmFileosDirectoryService mdmFileosDirectoryService;

    @GetMapping(Route.DIRECTORY_LIST)
    public R<List<MdmFileosDirectory>> list(@RequestParam String parentPath, @RequestParam(required = false) String bucketName) {
        List<MdmFileosDirectory> list = mdmFileosDirectoryService.getDirectoryList(parentPath, bucketName, null);
        return R.ok(list);
    }

    @GetMapping(Route.DIRECTORY_TREE)
    public R<List<MdmFileosDirectoryDto>> tree(@RequestParam(required = false) String bucketName) {
        List<MdmFileosDirectory> directories = mdmFileosDirectoryService.getDirectoryTree(bucketName, null);
        List<MdmFileosDirectoryDto> tree = buildTree(directories);
        return R.ok(tree);
    }

    @GetMapping(Route.DIRECTORY_INFO)
    public R<MdmFileosDirectory> info(@RequestParam String dirPath, @RequestParam(required = false) String bucketName) {
        MdmFileosDirectory directory = mdmFileosDirectoryService.getDirectoryByPath(dirPath, bucketName, null);
        return R.ok(directory);
    }

    private List<MdmFileosDirectoryDto> buildTree(List<MdmFileosDirectory> directories) {
        List<MdmFileosDirectoryDto> roots = new java.util.ArrayList<>();
        java.util.Map<String, MdmFileosDirectoryDto> map = new java.util.LinkedHashMap<>();
        for (MdmFileosDirectory dir : directories) {
            MdmFileosDirectoryDto dto = new MdmFileosDirectoryDto();
            MdmFileosDirectory.copy(dir, dto);
            dto.setChildren(new java.util.ArrayList<>());
            map.put(dir.getDirPath(), dto);
        }
        for (MdmFileosDirectory dir : directories) {
            MdmFileosDirectoryDto dto = map.get(dir.getDirPath());
            if (dir.getParentPath() == null || !map.containsKey(dir.getParentPath())) {
                roots.add(dto);
            } else {
                MdmFileosDirectoryDto parent = map.get(dir.getParentPath());
                parent.getChildren().add(dto);
            }
        }
        return roots;
    }

}
