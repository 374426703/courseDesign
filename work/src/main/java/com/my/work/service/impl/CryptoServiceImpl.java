package com.my.work.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.crypto.symmetric.AES;
import com.my.work.service.CryptoService;
import com.my.work.service.DecryptResult;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class CryptoServiceImpl implements CryptoService {

    private final RSA rsa;
    private final Digester digester;
    private final AES aes;

    public CryptoServiceImpl() {
        this.rsa = SecureUtil.rsa();
        this.digester = new Digester(DigestAlgorithm.SHA256);
        byte[] aesKey = SecureUtil.generateKey("AES").getEncoded();
        this.aes = SecureUtil.aes(aesKey);
    }

    @Override
    public String generateFingerprint(String plainText) {
        return digester.digestHex(plainText);
    }

    @Override
    public String generateSignature(String fingerprint) {
        byte[] encrypted = rsa.encrypt(fingerprint.getBytes(StandardCharsets.UTF_8), KeyType.PrivateKey);
        return Base64.encode(encrypted);
    }

    @Override
    public String symmetricEncrypt(String plainText, String signature) {
        String combined = plainText + "||SIGNATURE||" + signature;
        return aes.encryptBase64(combined);
    }

    @Override
    public DecryptResult symmetricDecrypt(String cipherText) {
        String decrypted = aes.decryptStr(cipherText);
        String[] parts = decrypted.split("\\|\\|SIGNATURE\\|\\|", 2);
        DecryptResult result = new DecryptResult();
        if (parts.length == 2) {
            result.setPlainText(parts[0]);
            result.setSignature(parts[1]);
        } else {
            result.setPlainText(parts[0]);
            result.setSignature("");
        }
        return result;
    }

    @Override
    public boolean verifySignature(String plainText, String signature) {
        byte[] decryptedBytes = rsa.decrypt(Base64.decode(signature), KeyType.PublicKey);
        String originalFingerprint = new String(decryptedBytes, StandardCharsets.UTF_8);
        String newFingerprint = generateFingerprint(plainText);
        return originalFingerprint.equals(newFingerprint);
    }
}
