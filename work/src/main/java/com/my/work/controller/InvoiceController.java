package com.my.work.controller;

import com.my.work.dto.Result;
import com.my.work.entity.Invoice;
import com.my.work.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/save")
    public Result<Long> saveInvoice(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        Map<String, Object> invoiceData = (Map<String, Object>) body.get("invoiceData");
        String xmlContent = (String) body.get("xmlContent");
        String fingerprint = (String) body.get("digitalFingerprint");
        String signature = (String) body.get("digitalSignature");
        Long id = invoiceService.saveInvoice(invoiceData, xmlContent, fingerprint, signature);
        return Result.success("发票保存成功，ID: " + id, id);
    }

    @GetMapping("/list")
    public Result<List<Invoice>> listInvoices() {
        return Result.success(invoiceService.listInvoices());
    }

    /** 删除发票 */
    @DeleteMapping("/{id}")
    public Result<Void> deleteInvoice(@PathVariable Long id) {
        boolean ok = invoiceService.deleteInvoice(id);
        if (ok) return Result.success("删除成功", null);
        return Result.error("删除失败，发票不存在");
    }

    /** 更新发票 */
    @PutMapping("/{id}")
    public Result<Void> updateInvoice(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        boolean ok = invoiceService.updateInvoice(id, data);
        if (ok) return Result.success("更新成功", null);
        return Result.error("更新失败，发票不存在");
    }
}
