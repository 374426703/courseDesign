package com.my.work.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_block")
public class Block {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer blockIndex;
    private String previousHash;
    private String hash;
    private String data;
    private Long timestamp;
    private Integer nonce;
    private Long invoiceId;
    private LocalDateTime createTime;
}
