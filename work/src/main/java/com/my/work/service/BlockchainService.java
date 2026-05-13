package com.my.work.service;

import com.my.work.entity.Block;

import java.util.List;
import java.util.Map;

public interface BlockchainService {

    /** 将发票数据上链 */
    Block addBlock(Long invoiceId, Map<String, Object> invoiceData);

    /** 获取整条链 */
    List<Block> getChain();

    /** 验证链的完整性 */
    boolean isChainValid();

    /** 获取链上某个区块的发票详情 */
    Map<String, Object> getBlockDetail(Long blockId);
}
