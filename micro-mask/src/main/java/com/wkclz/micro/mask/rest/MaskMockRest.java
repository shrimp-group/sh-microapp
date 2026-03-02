package com.wkclz.micro.mask.rest;

import com.alibaba.fastjson2.JSONObject;
import com.wkclz.core.base.R;
import com.wkclz.micro.mask.config.MaskResponseAdvice;
import com.wkclz.micro.mask.pojo.dto.MdmMaskRuleDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_mask_rule (脱敏规则) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
@RequestMapping(Route.PREFIX)
public class MaskMockRest {

    /**
     * @api {post} /micro-mask/rule/test 1. 脱敏规则-测试
     * @apiGroup MASK
     *
     * @apiVersion 0.0.1
     * @apiDescription 脱敏规则-测试
     *
     * @apiParam {Long} [id] <code>body</code>数据id
     *
     * @apiParamExample {json} 请求样例:
     * {
     *     "id": 1
     * }
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": 1
     * }
     *
     */
    @PostMapping(Route.RULE_TEST)
    public R maskRuleTest(@RequestBody MdmMaskRuleDto dto) {
        String mockValue = dto.getMockValue();
        if (StringUtils.isBlank(mockValue)) {
            return R.warn("做脱敏测试时，示例值不能为空！");
        }
        String maskValue = MaskResponseAdvice.maskByString(mockValue, dto);
        dto.setMaskValue(maskValue);
        dto.setMaskType("使用兜底规则进行脱敏！");
        if (StringUtils.isNotBlank(dto.getMaskRuleScript())) {
            dto.setMaskType("使用JS脚本进行脱敏！");
        }
        if (StringUtils.isNotBlank(dto.getMaskRuleRegular())) {
            dto.setMaskType("使用正则表达式进行匹配脱敏！");
        }
        return R.ok(dto);
    }




    /**
     * @api {get} /micro-mask/rule/verify 2. 脱敏规则-验证
     * @apiGroup MASK
     *
     * @apiVersion 0.0.1
     * @apiDescription 脱敏规则-验证
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {}
     * }
     *
     */
    @GetMapping(Route.RULE_VERIFY)
    public Object maskRuleVerify() {
        JSONObject jsonObject = mockJson();
        return R.ok(jsonObject);
    }

    private static JSONObject mockJson() {
        String jsonStr = """
            {
                "rows": [
                    {
                        "mobile2": "13812342222",
                        "mobile3": [
                            "13812343333",
                            "13812343333"
                        ],
                        "children": [
                            {
                                "age": 18,
                                "mobile4": "13812344444",
                                "mobile5": [
                                    [
                                        "13812345555",
                                        "13812345555"
                                    ],
                                    [
                        
                                    ]
                                ]
                            },
                            {
                                "age": 18,
                                "mobile4": "13812344444"
                            }
                        ]
                    },
                    {
                        "mobile2": "13812342222",
                        "mobile3": [
                            "13812343333",
                            "13812343333"
                        ],
                        "children": [
                            {
                                "age": 18,
                                "mobile4": "13812344444"
                            },
                            {
                                "age": 18,
                                "mobile4": "13812344444"
                            }
                        ]
                    }
                ],
                "current": 1,
                "size": 20,
                "total": 2,
                "page": 1,
                "mobile0": 13812340000,
                "mobile1": "13812341111"
            }
            """;
        return JSONObject.parseObject(jsonStr);
    }


}

