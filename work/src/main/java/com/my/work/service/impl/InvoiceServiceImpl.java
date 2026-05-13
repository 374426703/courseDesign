package com.my.work.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.my.work.dto.VerifyResponse;
import com.my.work.entity.Invoice;
import com.my.work.mapper.InvoiceMapper;
import com.my.work.service.CryptoService;
import com.my.work.service.DecryptResult;
import com.my.work.service.InvoiceService;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private InvoiceMapper invoiceMapper;

    @Override
    public Map<String, String> signXml(String xmlContent) {
        Map<String, String> result = new HashMap<>();
        String fingerprint = cryptoService.generateFingerprint(xmlContent);
        result.put("digitalFingerprint", fingerprint);
        String signature = cryptoService.generateSignature(fingerprint);
        result.put("digitalSignature", signature);
        String cipherText = cryptoService.symmetricEncrypt(xmlContent, signature);
        result.put("cipherText", cipherText);
        return result;
    }

    @Override
    public VerifyResponse verifyAndDecrypt(String cipherText, String digitalSignature) {
        VerifyResponse response = new VerifyResponse();
        DecryptResult decryptResult = cryptoService.symmetricDecrypt(cipherText);
        String plainText = decryptResult.getPlainText();
        String newFingerprint = cryptoService.generateFingerprint(plainText);
        boolean verified = cryptoService.verifySignature(plainText, digitalSignature);

        response.setSuccess(verified);
        response.setDigitalFingerprint(newFingerprint);

        if (verified) {
            Map<String, Object> invoiceData = parseXml(plainText);
            response.setInvoiceData(invoiceData);
            response.setMessage("验证成功！电子发票完整性和发送者身份已确认。");
        } else {
            response.setMessage("验证失败！数字指纹不匹配，发票可能被篡改。");
        }
        return response;
    }

    @Override
    public Map<String, Object> parseXml(String xmlContent) {
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            Document document = DocumentHelper.parseText(xmlContent);
            Element root = document.getRootElement();
            data.put("invoiceNumber", getElementText(root, "invoiceNumber"));
            data.put("invoiceCode", getElementText(root, "invoiceCode"));
            data.put("sellerName", getElementText(root, "sellerName"));
            data.put("sellerTaxNo", getElementText(root, "sellerTaxNo"));
            data.put("buyerName", getElementText(root, "buyerName"));
            data.put("buyerTaxNo", getElementText(root, "buyerTaxNo"));
            data.put("amount", getElementText(root, "amount"));
            data.put("taxAmount", getElementText(root, "taxAmount"));
            data.put("totalAmount", getElementText(root, "totalAmount"));
            data.put("invoiceDate", getElementText(root, "invoiceDate"));
            data.put("invoiceType", getElementText(root, "invoiceType"));
            data.put("remark", getElementText(root, "remark"));
        } catch (DocumentException e) {
            throw new RuntimeException("XML解析失败: " + e.getMessage(), e);
        }
        return data;
    }

    private String getElementText(Element parent, String elementName) {
        Element element = parent.element(elementName);
        return element != null ? element.getTextTrim() : null;
    }

    @Override
    public Long saveInvoice(Map<String, Object> invoiceData, String xmlContent,
                            String fingerprint, String signature) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber((String) invoiceData.get("invoiceNumber"));
        invoice.setInvoiceCode((String) invoiceData.get("invoiceCode"));
        invoice.setSellerName((String) invoiceData.get("sellerName"));
        invoice.setSellerTaxNo((String) invoiceData.get("sellerTaxNo"));
        invoice.setBuyerName((String) invoiceData.get("buyerName"));
        invoice.setBuyerTaxNo((String) invoiceData.get("buyerTaxNo"));
        invoice.setInvoiceType((String) invoiceData.get("invoiceType"));
        invoice.setRemark((String) invoiceData.get("remark"));

        try {
            String amountStr = (String) invoiceData.get("amount");
            if (StrUtil.isNotBlank(amountStr)) invoice.setAmount(new BigDecimal(amountStr));
            String taxStr = (String) invoiceData.get("taxAmount");
            if (StrUtil.isNotBlank(taxStr)) invoice.setTaxAmount(new BigDecimal(taxStr));
            String totalStr = (String) invoiceData.get("totalAmount");
            if (StrUtil.isNotBlank(totalStr)) invoice.setTotalAmount(new BigDecimal(totalStr));
        } catch (NumberFormatException ignored) {}

        try {
            String dateStr = (String) invoiceData.get("invoiceDate");
            if (StrUtil.isNotBlank(dateStr)) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                invoice.setInvoiceDate(LocalDateTime.parse(dateStr, fmt));
            }
        } catch (Exception ignored) {}

        invoice.setXmlContent(xmlContent);
        invoice.setDigitalFingerprint(fingerprint);
        invoice.setDigitalSignature(signature);
        invoice.setStatus(1);
        invoice.setCreateTime(LocalDateTime.now());
        invoice.setUpdateTime(LocalDateTime.now());

        invoiceMapper.insert(invoice);
        return invoice.getId();
    }

    @Override
    public List<Invoice> listInvoices() {
        QueryWrapper<Invoice> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        return invoiceMapper.selectList(wrapper);
    }

    @Override
    public boolean deleteInvoice(Long id) {
        return invoiceMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateInvoice(Long id, Map<String, Object> data) {
        Invoice invoice = invoiceMapper.selectById(id);
        if (invoice == null) return false;
        if (data.containsKey("sellerName")) invoice.setSellerName((String) data.get("sellerName"));
        if (data.containsKey("buyerName")) invoice.setBuyerName((String) data.get("buyerName"));
        if (data.containsKey("remark")) invoice.setRemark((String) data.get("remark"));
        if (data.containsKey("invoiceType")) invoice.setInvoiceType((String) data.get("invoiceType"));
        invoice.setUpdateTime(LocalDateTime.now());
        return invoiceMapper.updateById(invoice) > 0;
    }
}
