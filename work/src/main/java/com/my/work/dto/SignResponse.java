package com.my.work.dto;

import lombok.Data;

@Data
public class SignResponse {
    private String cipherText;
    private String digitalSignature;
    private String digitalFingerprint;
}
