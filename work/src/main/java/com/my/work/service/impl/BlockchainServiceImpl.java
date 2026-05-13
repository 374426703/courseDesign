package com.my.work.service.impl;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.my.work.entity.Block;
import com.my.work.entity.Invoice;
import com.my.work.mapper.BlockMapper;
import com.my.work.mapper.InvoiceMapper;
import com.my.work.service.BlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class BlockchainServiceImpl implements BlockchainService {

    @Autowired
    private BlockMapper blockMapper;

    @Autowired
    private InvoiceMapper invoiceMapper;

    @Value("${blockchain.difficulty:4}")
    private int difficulty;

    private final Digester digester = new Digester(DigestAlgorithm.SHA256);

    @Override
    public Block addBlock(Long invoiceId, Map<String, Object> invoiceData) {
        QueryWrapper<Block> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("block_index").last("LIMIT 1");
        Block lastBlock = blockMapper.selectOne(wrapper);

        int newIndex = (lastBlock != null) ? lastBlock.getBlockIndex() + 1 : 1;
        String previousHash = (lastBlock != null) ? lastBlock.getHash() : "0";
        long timestamp = System.currentTimeMillis();
        String data = invoiceData.toString();

        int nonce = 0;
        String hash;
        String targetPrefix = "0".repeat(difficulty);
        do {
            nonce++;
            hash = calculateHash(newIndex, previousHash, data, timestamp, nonce);
        } while (!hash.startsWith(targetPrefix));

        Block block = new Block();
        block.setBlockIndex(newIndex);
        block.setPreviousHash(previousHash);
        block.setHash(hash);
        block.setData(data);
        block.setTimestamp(timestamp);
        block.setNonce(nonce);
        block.setInvoiceId(invoiceId);
        block.setCreateTime(LocalDateTime.now());
        blockMapper.insert(block);

        Invoice invoice = invoiceMapper.selectById(invoiceId);
        if (invoice != null) {
            invoice.setBlockHash(hash);
            invoice.setUpdateTime(LocalDateTime.now());
            invoiceMapper.updateById(invoice);
        }

        return block;
    }

    private String calculateHash(int index, String previousHash, String data, long timestamp, int nonce) {
        String input = index + previousHash + data + timestamp + nonce;
        return digester.digestHex(input);
    }

    @Override
    public List<Block> getChain() {
        QueryWrapper<Block> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("block_index");
        return blockMapper.selectList(wrapper);
    }

    @Override
    public boolean isChainValid() {
        List<Block> chain = getChain();
        for (int i = 1; i < chain.size(); i++) {
            Block current = chain.get(i);
            Block previous = chain.get(i - 1);
            String calculatedHash = calculateHash(
                    current.getBlockIndex(), current.getPreviousHash(),
                    current.getData(), current.getTimestamp(), current.getNonce());
            if (!calculatedHash.equals(current.getHash())) return false;
            if (!current.getPreviousHash().equals(previous.getHash())) return false;
        }
        return true;
    }

    @Override
    public Map<String, Object> getBlockDetail(Long blockId) {
        Block block = blockMapper.selectById(blockId);
        if (block == null) return null;
        Map<String, Object> detail = new HashMap<>();
        detail.put("block", block);
        if (block.getInvoiceId() != null) {
            Invoice invoice = invoiceMapper.selectById(block.getInvoiceId());
            detail.put("invoice", invoice);
        }
        return detail;
    }
}
