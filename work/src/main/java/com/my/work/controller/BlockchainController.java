package com.my.work.controller;

import com.my.work.dto.Result;
import com.my.work.entity.Block;
import com.my.work.service.BlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blockchain")
public class BlockchainController {

    @Autowired
    private BlockchainService blockchainService;

    /** 将发票上链 */
    @PostMapping("/add")
    public Result<Block> addBlock(@RequestBody Map<String, Object> body) {
        Long invoiceId = Long.valueOf(body.get("invoiceId").toString());
        @SuppressWarnings("unchecked")
        Map<String, Object> invoiceData = (Map<String, Object>) body.get("invoiceData");
        Block block = blockchainService.addBlock(invoiceId, invoiceData);
        return Result.success("区块添加成功", block);
    }

    /** 获取整条链 */
    @GetMapping("/chain")
    public Result<List<Block>> getChain() {
        return Result.success(blockchainService.getChain());
    }

    /** 验证链的完整性 */
    @GetMapping("/validate")
    public Result<Boolean> validateChain() {
        boolean valid = blockchainService.isChainValid();
        return Result.success(valid ? "区块链数据完整" : "区块链数据已被篡改！", valid);
    }

    /** 获取区块详情 */
    @GetMapping("/block/{id}")
    public Result<Map<String, Object>> getBlockDetail(@PathVariable Long id) {
        Map<String, Object> detail = blockchainService.getBlockDetail(id);
        if (detail == null) return Result.error("区块不存在");
        return Result.success(detail);
    }
}
