package com.my.work.service;

import com.my.work.dto.VerifyResponse;
import com.my.work.entity.Invoice;

import java.util.List;
import java.util.Map;

public interface InvoiceService {

    Map<String, String> signXml(String xmlContent);

    VerifyResponse verifyAndDecrypt(String cipherText, String digitalSignature);

    Map<String, Object> parseXml(String xmlContent);

    Long saveInvoice(Map<String, Object> invoiceData, String xmlContent, String fingerprint, String signature);

    List<Invoice> listInvoices();

    boolean deleteInvoice(Long id);

    boolean updateInvoice(Long id, Map<String, Object> data);
}
