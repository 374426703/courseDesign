package com.my.work.dto;

import lombok.Data;

import java.util.Map;

@Data
public class OcrResponse {
    private String xmlContent;
    private Map<String, String> ocrData;
}
