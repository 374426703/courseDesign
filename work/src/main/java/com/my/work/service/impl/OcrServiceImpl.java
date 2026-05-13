package com.my.work.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.my.work.service.OcrService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class OcrServiceImpl implements OcrService {

    @Value("${baidu-ocr.api-key:}")
    private String apiKey;

    @Value("${baidu-ocr.secret-key:}")
    private String secretKey;

    private final AtomicReference<String> accessToken = new AtomicReference<>(null);
    private long tokenExpireTime = 0;

    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String OCR_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/vat_invoice";

    /**
     * 获取百度OCR access_token（自动缓存和刷新）
     */
    private String getAccessToken() {
        long now = System.currentTimeMillis();
        String token = accessToken.get();
        if (token != null && now < tokenExpireTime - 60000) {
            return token;
        }

        String response = HttpRequest.post(TOKEN_URL)
                .form("grant_type", "client_credentials")
                .form("client_id", apiKey)
                .form("client_secret", secretKey)
                .timeout(10000)
                .execute()
                .body();

        JSONObject json = JSONUtil.parseObj(response);
        token = json.getStr("access_token");
        int expiresIn = json.getInt("expires_in", 2592000);
        accessToken.set(token);
        tokenExpireTime = now + expiresIn * 1000L;
        return token;
    }

    @Override
    public Map<String, String> recognizeInvoice(byte[] imageBytes) {
        // 调用百度OCR识别发票
        String imageBase64 = Base64.encode(imageBytes);
        String token = getAccessToken();

        HttpResponse response = HttpRequest.post(OCR_URL + "?access_token=" + URLEncoder.encode(token, StandardCharsets.UTF_8))
                .form("image", imageBase64)
                .timeout(30000)
                .execute();

        String body = response.body();
        JSONObject result = JSONUtil.parseObj(body);

        // 检查错误
        if (result.containsKey("error_code")) {
            String errorMsg = result.getStr("error_msg", "未知错误");
            throw new RuntimeException("百度OCR识别失败: " + errorMsg + " (code: " + result.getInt("error_code") + ")");
        }

        JSONObject wordsResult = result.getJSONObject("words_result");
        if (wordsResult == null) {
            throw new RuntimeException("OCR识别结果为空，请检查图片是否清晰");
        }

        // 提取关键字段
        Map<String, String> invoiceData = new HashMap<>();
        putIfExists(wordsResult, invoiceData, "InvoiceCode", "invoiceCode");
        putIfExists(wordsResult, invoiceData, "InvoiceNum", "invoiceNumber");
        putIfExists(wordsResult, invoiceData, "InvoiceDate", "invoiceDate");
        putIfExists(wordsResult, invoiceData, "SellerName", "sellerName");
        putIfExists(wordsResult, invoiceData, "SellerRegisterNum", "sellerTaxNo");
        putIfExists(wordsResult, invoiceData, "PurchaserName", "buyerName");
        putIfExists(wordsResult, invoiceData, "PurchaserRegisterNum", "buyerTaxNo");
        putIfExists(wordsResult, invoiceData, "AmountInFigu", "amount");
        putIfExists(wordsResult, invoiceData, "Tax", "taxAmount");
        putIfExists(wordsResult, invoiceData, "TotalAmount", "totalAmount");
        putIfExists(wordsResult, invoiceData, "InvoiceType", "invoiceType");
        putIfExists(wordsResult, invoiceData, "Remarks", "remark");

        return invoiceData;
    }

    private void putIfExists(JSONObject source, Map<String, String> target, String baiduKey, String ourKey) {
        if (source.containsKey(baiduKey) && source.get(baiduKey) != null) {
            target.put(ourKey, source.getStr(baiduKey));
        }
    }

    @Override
    public String imageToXml(byte[] fileBytes, String fileName) {
        Map<String, String> data = recognizeInvoice(fileBytes);

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<invoice>\n");
        xml.append("    <invoiceNumber>").append(escapeXml(data.getOrDefault("invoiceNumber", ""))).append("</invoiceNumber>\n");
        xml.append("    <invoiceCode>").append(escapeXml(data.getOrDefault("invoiceCode", ""))).append("</invoiceCode>\n");
        xml.append("    <sellerName>").append(escapeXml(data.getOrDefault("sellerName", ""))).append("</sellerName>\n");
        xml.append("    <sellerTaxNo>").append(escapeXml(data.getOrDefault("sellerTaxNo", ""))).append("</sellerTaxNo>\n");
        xml.append("    <buyerName>").append(escapeXml(data.getOrDefault("buyerName", ""))).append("</buyerName>\n");
        xml.append("    <buyerTaxNo>").append(escapeXml(data.getOrDefault("buyerTaxNo", ""))).append("</buyerTaxNo>\n");
        xml.append("    <amount>").append(escapeXml(data.getOrDefault("amount", ""))).append("</amount>\n");
        xml.append("    <taxAmount>").append(escapeXml(data.getOrDefault("taxAmount", ""))).append("</taxAmount>\n");
        xml.append("    <totalAmount>").append(escapeXml(data.getOrDefault("totalAmount", ""))).append("</totalAmount>\n");
        xml.append("    <invoiceDate>").append(escapeXml(data.getOrDefault("invoiceDate", ""))).append("</invoiceDate>\n");
        xml.append("    <invoiceType>").append(escapeXml(data.getOrDefault("invoiceType", "增值税电子发票"))).append("</invoiceType>\n");
        xml.append("    <remark>").append(escapeXml(data.getOrDefault("remark", "OCR自动识别"))).append("</remark>\n");
        xml.append("</invoice>");

        return xml.toString();
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
