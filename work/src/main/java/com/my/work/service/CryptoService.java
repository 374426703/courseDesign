package com.my.work.service;

public interface CryptoService {

    /** 生成数字指纹：对明文使用hash算法 */
    String generateFingerprint(String plainText);

    /** 生成数字签名：对数字指纹使用非对称加密（私钥加密） */
    String generateSignature(String fingerprint);

    /** 对称加密：对明文+数字签名进行加密传输 */
    String symmetricEncrypt(String plainText, String signature);

    /** 对称解密：解密密文得到明文+数字签名 */
    DecryptResult symmetricDecrypt(String cipherText);

    /** 验证数字签名：用公钥解密签名得到指纹，与原文hash对比 */
    boolean verifySignature(String plainText, String signature);
}
