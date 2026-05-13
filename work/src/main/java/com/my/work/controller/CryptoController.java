package com.my.work.controller;

import com.my.work.dto.*;
import com.my.work.service.CryptoService;
import com.my.work.service.DecryptResult;
import com.my.work.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/crypto")
public class CryptoController {

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private InvoiceService invoiceService;

    /** 发送方：对XML进行完整签名流程 */
    @PostMapping("/sign")
    public Result<Map<String, String>> sign(@RequestBody SignRequest request) {
        Map<String, String> result = invoiceService.signXml(request.getXmlContent());
        return Result.success(result);
    }

    /** 接收方：解密并验证签名 */
    @PostMapping("/verify")
    public Result<VerifyResponse> verify(@RequestBody VerifyRequest request) {
        VerifyResponse response = invoiceService.verifyAndDecrypt(
                request.getCipherText(), request.getDigitalSignature());
        if (response.isSuccess()) return Result.success(response);
        return Result.error(response.getMessage());
    }

    /** 单独生成数字指纹 */
    @PostMapping("/fingerprint")
    public Result<String> generateFingerprint(@RequestBody Map<String, String> body) {
        String fingerprint = cryptoService.generateFingerprint(body.get("content"));
        return Result.success(fingerprint);
    }

    /** 单独生成数字签名 */
    @PostMapping("/signature")
    public Result<String> generateSignature(@RequestBody Map<String, String> body) {
        String signature = cryptoService.generateSignature(body.get("fingerprint"));
        return Result.success(signature);
    }

    /** 对称加密 */
    @PostMapping("/encrypt")
    public Result<String> encrypt(@RequestBody EncryptRequest request) {
        String cipherText = cryptoService.symmetricEncrypt(
                request.getPlainText(), request.getDigitalSignature());
        return Result.success(cipherText);
    }

    /** 对称解密 */
    @PostMapping("/decrypt")
    public Result<DecryptResult> decrypt(@RequestBody DecryptRequest request) {
        DecryptResult result = cryptoService.symmetricDecrypt(request.getCipherText());
        return Result.success(result);
    }

    /** 解析XML */
    @PostMapping("/parse-xml")
    public Result<Map<String, Object>> parseXml(@RequestBody Map<String, String> body) {
        Map<String, Object> data = invoiceService.parseXml(body.get("xmlContent"));
        return Result.success(data);
    }
}
