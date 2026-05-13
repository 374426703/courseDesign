<template>
  <div class="invoice-page">
    <div class="page-hero">
      <div class="hero-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M9 3v18M3 9h6M3 15h6"/></svg>
      </div>
      <div>
        <h2>发票管理</h2>
        <p>查看、编辑、删除已验证的电子发票记录，管理所有发票数据</p>
      </div>
      <div class="hero-actions">
        <el-button size="large" @click="loadInvoices" :loading="loading" class="btn-outline">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <span class="stat-num">{{ invoices.length }}</span>
        <span class="stat-label">发票总数</span>
      </div>
      <div class="stat-card verified">
        <span class="stat-num">{{ verifiedCount }}</span>
        <span class="stat-label">已验证</span>
      </div>
      <div class="stat-card chained">
        <span class="stat-num">{{ chainedCount }}</span>
        <span class="stat-label">已上链</span>
      </div>
    </div>

    <div class="table-card">
      <el-table :data="invoices" v-loading="loading" stripe class="invoice-table" empty-text="暂无发票数据，请先在接收方页面验证并保存发票" table-layout="auto">
        <el-table-column prop="id" label="ID" min-width="50" align="center" />
        <el-table-column prop="invoiceNumber" label="发票号码" min-width="140">
          <template #default="{ row }">
            <span class="cell-code">{{ row.invoiceNumber }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="invoiceCode" label="发票代码" min-width="130">
          <template #default="{ row }">
            <span class="cell-code">{{ row.invoiceCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sellerName" label="销售方" min-width="120" show-overflow-tooltip />
        <el-table-column prop="buyerName" label="购买方" min-width="120" show-overflow-tooltip />
        <el-table-column prop="totalAmount" label="价税合计" min-width="100" align="right">
          <template #default="{ row }">
            <span class="cell-amount">{{ formatMoney(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="invoiceType" label="发票类型" min-width="110" show-overflow-tooltip />
        <el-table-column label="验证" align="center" min-width="60">
          <template #default="{ row }">
            <span class="status-dot-inline" :class="row.status === 1 ? 'ok' : 'pending'" :title="row.status === 1 ? '已验证' : '未验证'"></span>
          </template>
        </el-table-column>
        <el-table-column label="链" align="center" min-width="50">
          <template #default="{ row }">
            <span class="chain-dot-inline" :class="row.blockHash ? 'on' : 'off'" :title="row.blockHash ? '已上链' : '未上链'"></span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="140" />
        <el-table-column label="操作" min-width="160">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button size="small" text type="primary" @click="viewDetail(row)">详情</el-button>
              <el-button size="small" text type="warning" @click="editInvoice(row)">编辑</el-button>
              <el-button size="small" text type="danger" @click="handleDelete(row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="发票详情" width="680px" destroy-on-close>
      <div v-if="currentInvoice" class="detail-grid">
        <div class="d-item"><span class="d-label">发票号码</span><span class="d-value">{{ currentInvoice.invoiceNumber }}</span></div>
        <div class="d-item"><span class="d-label">发票代码</span><span class="d-value">{{ currentInvoice.invoiceCode }}</span></div>
        <div class="d-item"><span class="d-label">销售方名称</span><span class="d-value">{{ currentInvoice.sellerName }}</span></div>
        <div class="d-item"><span class="d-label">销售方税号</span><span class="d-value">{{ currentInvoice.sellerTaxNo }}</span></div>
        <div class="d-item"><span class="d-label">购买方名称</span><span class="d-value">{{ currentInvoice.buyerName }}</span></div>
        <div class="d-item"><span class="d-label">购买方税号</span><span class="d-value">{{ currentInvoice.buyerTaxNo }}</span></div>
        <div class="d-item"><span class="d-label">金额</span><span class="d-value">{{ formatMoney(currentInvoice.amount) }}</span></div>
        <div class="d-item"><span class="d-label">税额</span><span class="d-value">{{ formatMoney(currentInvoice.taxAmount) }}</span></div>
        <div class="d-item"><span class="d-label">价税合计</span><span class="d-value fw-bold">{{ formatMoney(currentInvoice.totalAmount) }}</span></div>
        <div class="d-item"><span class="d-label">开票日期</span><span class="d-value">{{ currentInvoice.invoiceDate }}</span></div>
        <div class="d-item"><span class="d-label">发票类型</span><span class="d-value">{{ currentInvoice.invoiceType }}</span></div>
        <div class="d-item"><span class="d-label">备注</span><span class="d-value">{{ currentInvoice.remark }}</span></div>
        <div class="d-item full"><span class="d-label">数字指纹</span><span class="d-value mono">{{ currentInvoice.digitalFingerprint }}</span></div>
        <div class="d-item full"><span class="d-label">区块链 Hash</span><span class="d-value mono" :class="currentInvoice.blockHash ? 'text-green' : 'text-gray'">{{ currentInvoice.blockHash || '未上链' }}</span></div>
      </div>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑发票" width="480px" destroy-on-close>
      <div v-if="currentInvoice" class="edit-form">
        <div class="form-group">
          <label>销售方名称</label>
          <el-input v-model="editForm.sellerName" />
        </div>
        <div class="form-group">
          <label>购买方名称</label>
          <el-input v-model="editForm.buyerName" />
        </div>
        <div class="form-group">
          <label>发票类型</label>
          <el-input v-model="editForm.invoiceType" />
        </div>
        <div class="form-group">
          <label>备注</label>
          <el-input v-model="editForm.remark" type="textarea" :rows="3" />
        </div>
      </div>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEdit" :loading="editLoading">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const invoices = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const editVisible = ref(false)
const editLoading = ref(false)
const currentInvoice = ref(null)
const editForm = ref({})

const verifiedCount = computed(() => invoices.value.filter(i => i.status === 1).length)
const chainedCount = computed(() => invoices.value.filter(i => i.blockHash).length)

onMounted(() => loadInvoices())

async function loadInvoices() {
  loading.value = true
  try {
    const res = await api.listInvoices()
    invoices.value = Array.isArray(res.data.data) ? res.data.data : (Array.isArray(res.data) ? res.data : [])
  } catch (e) {
    const msg = e.response?.data?.message || e.response?.statusText || e.message
    ElMessage.error('加载发票失败: ' + msg)
    console.error('Invoice load error:', e)
  }
  finally { loading.value = false }
}

function viewDetail(row) { currentInvoice.value = row; detailVisible.value = true }
function editInvoice(row) {
  currentInvoice.value = row
  editForm.value = { sellerName: row.sellerName, buyerName: row.buyerName, invoiceType: row.invoiceType, remark: row.remark }
  editVisible.value = true
}
async function handleEdit() {
  editLoading.value = true
  try {
    await api.updateInvoice(currentInvoice.value.id, editForm.value)
    ElMessage.success('更新成功'); editVisible.value = false; loadInvoices()
  } catch { ElMessage.error('更新失败') }
  finally { editLoading.value = false }
}
async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除该发票记录吗？此操作不可恢复。', '确认删除', { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' })
    await api.deleteInvoice(id); ElMessage.success('已删除'); loadInvoices()
  } catch { /* cancelled */ }
}

function formatMoney(val) {
  if (val === null || val === undefined) return '-'
  return '¥' + Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
</script>

<style scoped>
.invoice-page { width: 100%; }

.page-hero { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
.hero-icon {
  width: 48px; height: 48px; border-radius: 12px;
  background: linear-gradient(135deg, #F59E0B, #D97706);
  display: flex; align-items: center; justify-content: center; color: #fff; flex-shrink: 0;
}
.hero-icon svg { width: 24px; height: 24px; }
.page-hero h2 { font-size: 20px; font-weight: 700; color: #1E293B; }
.page-hero p { font-size: 13px; color: #64748B; margin-top: 2px; }
.hero-actions { margin-left: auto; }
.btn-outline { border: 1px solid #E2E8F0; border-radius: 8px; background: #fff; display: inline-flex; align-items: center; gap: 6px; font-weight: 600; }

.stats-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 20px; }
.stat-card {
  background: #fff; border-radius: 12px; padding: 20px 24px; box-shadow: 0 1px 3px rgba(0,0,0,.04);
  display: flex; flex-direction: column;
}
.stat-num { font-size: 28px; font-weight: 700; color: #1E293B; }
.stat-label { font-size: 12px; color: #94A3B8; margin-top: 4px; }
.stat-card.verified { border-left: 3px solid #22C55E; }
.stat-card.chained { border-left: 3px solid #F59E0B; }

.table-card { background: #fff; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,.04); overflow: hidden; }
.invoice-table { width: 100% !important; }

.cell-code { font-family: 'JetBrains Mono', monospace; font-size: 12px; }
.cell-amount { font-family: 'JetBrains Mono', monospace; font-size: 13px; font-weight: 600; color: #1E293B; }

.status-dot-inline {
  display: inline-block; width: 8px; height: 8px; border-radius: 50%;
}
.status-dot-inline.ok { background: #22C55E; box-shadow: 0 0 6px rgba(34,197,94,.5); }
.status-dot-inline.pending { background: #CBD5E1; }

.chain-dot-inline {
  display: inline-block; width: 8px; height: 8px; border-radius: 50%;
}
.chain-dot-inline.on { background: #F59E0B; box-shadow: 0 0 6px rgba(245,158,11,.5); }
.chain-dot-inline.off { background: #CBD5E1; }

.row-actions { display: flex; gap: 0; white-space: nowrap; }

.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1px; background: #E2E8F0; border-radius: 8px; overflow: hidden; }
.d-item { background: #fff; padding: 12px 16px; }
.d-item.full { grid-column: span 2; }
.d-label { font-size: 11px; color: #94A3B8; display: block; margin-bottom: 2px; }
.d-value { font-size: 13px; font-weight: 600; color: #1E293B; word-break: break-all; }
.d-value.mono { font-family: 'JetBrains Mono', monospace; font-size: 11px; }
.text-green { color: #059669 !important; }
.text-gray { color: #94A3B8 !important; }
.fw-bold { font-weight: 700; color: #DC2626; }

.edit-form { display: flex; flex-direction: column; gap: 16px; }
.form-group label { display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px; color: #475569; }
</style>
