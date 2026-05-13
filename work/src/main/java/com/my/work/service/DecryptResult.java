package com.my.work.service;

import lombok.Data;

@Data
public class DecryptResult {
    private String plainText;
    private String signature;
}
