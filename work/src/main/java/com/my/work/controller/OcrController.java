package com.my.work.controller;

import com.my.work.dto.OcrResponse;
import com.my.work.dto.Result;
import com.my.work.service.InvoiceService;
import com.my.work.service.OcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ocr")
public class OcrController {

    @Autowired
    private OcrService ocrService;

    @Autowired
    private InvoiceService invoiceService;

    /**
     * 上传发票图片/PDF并转换为XML
     */
    @PostMapping("/convert")
    public Result<OcrResponse> convertToXml(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            return Result.error("文件名无效");
        }

        // 检查文件类型
        String lowerName = fileName.toLowerCase();
        boolean isValidType = lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")
                || lowerName.endsWith(".png") || lowerName.endsWith(".bmp")
                || lowerName.endsWith(".pdf");
        if (!isValidType) {
            return Result.error("不支持的文件格式，请上传 JPG/PNG/BMP 或 PDF 文件");
        }

        try {
            byte[] fileBytes = file.getBytes();
            // 限制文件大小（10MB）
            if (fileBytes.length > 10 * 1024 * 1024) {
                return Result.error("文件过大，请上传小于10MB的文件");
            }

            // OCR识别并转换为XML
            String xmlContent = ocrService.imageToXml(fileBytes, fileName);

            // 解析XML确认数据
            Map<String, Object> parsedData = invoiceService.parseXml(xmlContent);

            OcrResponse response = new OcrResponse();
            response.setXmlContent(xmlContent);
            Map<String, String> ocrData = new HashMap<>();
            parsedData.forEach((k, v) -> ocrData.put(k, v != null ? v.toString() : ""));
            response.setOcrData(ocrData);

            return Result.success("OCR识别成功", response);

        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("百度OCR")) {
                return Result.error(msg);
            }
            return Result.error("OCR识别失败: " + e.getMessage());
        }
    }
}
