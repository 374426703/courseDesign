package com.my.work.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_invoice")
public class Invoice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String invoiceNumber;
    private String invoiceCode;
    private String sellerName;
    private String sellerTaxNo;
    private String buyerName;
    private String buyerTaxNo;
    private BigDecimal amount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private LocalDateTime invoiceDate;
    private String invoiceType;
    private String remark;
    private String xmlContent;
    private String digitalFingerprint;
    private String digitalSignature;
    private Integer status;
    private String blockHash;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
