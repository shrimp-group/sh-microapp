package com.wkclz.micro.pdf.helper;

import com.alibaba.fastjson2.JSONObject;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.pdf.config.PdfConfig;
import com.wkclz.tool.utils.FileUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Component
public class PdfHelper {

    @Autowired
    private PdfConfig pdfConfig;

    public static Context getContext(String data) {
        Context context = new Context();
        if (StringUtils.isBlank(data)) {
            return context;
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(data);
            for (String key : jsonObject.keySet()) {
                context.setVariable(key, jsonObject.get(key));
            }
            return context;

        } catch (Exception e) {
            throw ValidationException.of("mock 数据不是 json 格式，请修正后重试");
        }
    }

    public static String thymeleafRenderer(String templateContext, Context context) {
        // 初始化Thymeleaf模板引擎
        TemplateEngine templateEngine = new TemplateEngine();
        // 加载HTML模板并处理参数
        String htmlContent = templateEngine.process(templateContext, context);
        if (htmlContent.contains("<body>")) {
            // body 指定宋体
            htmlContent = htmlContent.replace("<body>", "<body style=\"font-family: SimSun, sans-serif;\">");
        }
        return htmlContent;
    }

    public String pdfRenderer(String htmlContent) {

        // 使用XHTMLRenderer生成PDF
        ITextRenderer renderer = new ITextRenderer();

        // 加载字段路径
        String simsunPath = pdfConfig.getSimsunPath();
        if (StringUtils.isNotBlank(simsunPath) && PdfHelper.isUnix()) {
            ITextFontResolver fontResolver = renderer.getFontResolver();
            try {
                fontResolver.addFont(simsunPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } catch (DocumentException | IOException e) {
                log.error("add font error: {}", e.getMessage());
            }
        }
        renderer.setPDFVersion(PdfWriter.VERSION_1_7);
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();

        String pdfPath = FileUtil.getTempPath("pdf");
        String name = "pdf_" + System.currentTimeMillis() + ".pdf";
        String pdfFullPath = pdfPath + "/" + name;

        // 输出流
        OutputStream os = null;
        try {
            os = new FileOutputStream(pdfFullPath);
            renderer.createPDF(os);
        } catch (Exception e) {
            throw ValidationException.of("生成 pdf 失败: {}", e.getMessage());
        } finally {
            if (os != null) {
                try {
                    os.flush();
                } catch (IOException e) {
                    //
                }
                try {
                    os.close();
                } catch (IOException e) {
                    //
                }
            }
        }
        return pdfFullPath;
    }


    public static void pdfResponse(HttpServletResponse response, String pdfPath, String pdfName) {
        if (pdfName == null) {
            pdfName = "pdf_" + System.currentTimeMillis();
        }
        response.setHeader("Content-Disposition", "inline; filename=" + pdfName + ".pdf");
        // 设置响应类型
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        // 将PDF文件的内容写入响应输出流
        try {
            Files.copy(Paths.get(pdfPath), response.getOutputStream());
        } catch (IOException e) {
            log.error("response pdf error: {}", e.getMessage());
        }
    }


    public static boolean isUnix() {
        String os = System.getProperty("os.name");
        if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return true;
        }
        return false;
    }


}
