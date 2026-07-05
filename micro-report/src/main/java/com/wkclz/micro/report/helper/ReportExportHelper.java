package com.wkclz.micro.report.helper;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.wkclz.core.exception.ValidationException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 报表 Excel 导出工具
 */
@Slf4j
@Component
public class ReportExportHelper {

    /**
     * 导出报表数据为 Excel
     * @param response HTTP 响应
     * @param data 数据列表
     * @param headers 表头映射（key=字段编码, value=字段名称）
     * @param fileName 文件名
     */
    public void export(HttpServletResponse response, List<LinkedHashMap<String, Object>> data,
                       LinkedHashMap<String, String> headers, String fileName) {
        if (headers == null || headers.isEmpty()) {
            throw ValidationException.of("导出表头不能为空");
        }

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName + ".xlsx");

            // 构建表头列表和字段列表
            List<String> headerNames = new ArrayList<>(headers.values());
            List<String> fieldKeys = new ArrayList<>(headers.keySet());

            // 构建数据行
            List<List<Object>> dataList = new ArrayList<>();
            if (data != null) {
                for (LinkedHashMap<String, Object> row : data) {
                    List<Object> rowData = new ArrayList<>();
                    for (String key : fieldKeys) {
                        Object value = row.get(key);
                        rowData.add(value != null ? value.toString() : "");
                    }
                    dataList.add(rowData);
                }
            }

            // 写入 Excel
            EasyExcel.write(response.getOutputStream())
                .head(buildHead(headerNames))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet(fileName)
                .doWrite(dataList);

            log.info("报表导出成功: {}, 数据行数: {}", fileName, dataList.size());
        } catch (IOException e) {
            log.error("报表导出失败: {}", e.getMessage());
            throw new RuntimeException("报表导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建 EasyExcel 表头结构
     */
    private List<List<String>> buildHead(List<String> headerNames) {
        List<List<String>> head = new ArrayList<>();
        for (String name : headerNames) {
            List<String> columnHead = new ArrayList<>();
            columnHead.add(name);
            head.add(columnHead);
        }
        return head;
    }

}
