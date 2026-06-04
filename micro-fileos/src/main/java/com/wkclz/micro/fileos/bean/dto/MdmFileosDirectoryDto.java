package com.wkclz.micro.fileos.bean.dto;

import com.wkclz.micro.fileos.bean.entity.MdmFileosDirectory;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmFileosDirectoryDto extends MdmFileosDirectory {

    private List<MdmFileosDirectoryDto> children;
}
