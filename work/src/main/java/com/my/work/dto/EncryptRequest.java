package com.my.work.dto;

import lombok.Data;

@Data
public class EncryptRequest {
    private String plainText;
    private String digitalSignature;
}
