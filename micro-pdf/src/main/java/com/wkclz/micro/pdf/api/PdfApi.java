package com.wkclz.micro.pdf.api;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.pdf.cache.PdfTemplateCache;
import com.wkclz.micro.pdf.helper.PdfHelper;
import com.wkclz.micro.pdf.pojo.entity.MdmPdfTemplate;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

@Component
public class PdfApi {

    @Autowired
    private PdfHelper pdfHelper;
    @Autowired
    private PdfTemplateCache pdfTemplateCache;

    public void responsePdf(String templateCode, String data, HttpServletResponse response) {
        String pdfPath = writePdf(templateCode, data);
        PdfHelper.pdfResponse(response, pdfPath, null);
    }

    public String writePdf(String templateCode, String data) {
        MdmPdfTemplate template = pdfTemplateCache.getPdfTemplate(templateCode);
        if (template == null) {
            throw ValidationException.of("error templateCode: ", templateCode);
        }
        String templateContext = template.getTemplateContext();
        if (StringUtils.isBlank(templateContext)) {
            throw ValidationException.of("error templateContext of template: {}", templateCode);
        }
        Context context = PdfHelper.getContext(data);
        String htmlContent = PdfHelper.thymeleafRenderer(templateContext, context);
        return pdfHelper.pdfRenderer(htmlContent);
    }



}
