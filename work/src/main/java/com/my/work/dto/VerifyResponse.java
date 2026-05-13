package com.my.work.dto;

import lombok.Data;
import java.util.Map;

@Data
public class VerifyResponse {
    private boolean success;
    private String message;
    private Map<String, Object> invoiceData;
    private String digitalFingerprint;
}
