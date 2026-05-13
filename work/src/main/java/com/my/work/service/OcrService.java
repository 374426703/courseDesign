package com.my.work.service;

import java.util.Map;

public interface OcrService {

    /**
     * 将发票图片/PDF转换为XML格式
     * @param fileBytes 文件字节数组
     * @param fileName  原始文件名（用于判断文件类型）
     * @return 发票XML内容
     */
    String imageToXml(byte[] fileBytes, String fileName);

    /**
     * 调用百度OCR识别发票
     * @param imageBase64 图片Base64编码
     * @return OCR识别结果
     */
    Map<String, String> recognizeInvoice(byte[] imageBytes);
}
