package com.my.work.dto;

import lombok.Data;

@Data
public class VerifyRequest {
    private String cipherText;
    private String digitalSignature;
}
