<template>
  <div class="blockchain-page">
    <div class="page-hero">
      <div class="hero-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M8 12h8M12 8v8"/></svg>
      </div>
      <div>
        <h2>区块链管理</h2>
        <p>将验证通过的发票上链，浏览区块链数据，验证链的完整性</p>
      </div>
    </div>

    <!-- 验证 + 上链卡片 -->
    <div class="top-actions">
      <div class="action-card mint-card">
        <div class="action-card-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        </div>
        <div class="action-card-info">
          <div class="action-card-title">发票上链</div>
          <div class="action-card-desc">将已验证的发票数据写入区块链</div>
        </div>
        <div class="action-card-form">
          <el-input-number v-model="chainInvoiceId" :min="1" placeholder="发票ID" size="large" controls-position="right" />
          <el-button type="primary" size="large" @click="addToChain" :loading="adding" :disabled="!chainInvoiceId" class="btn-primary">
            确认上链
          </el-button>
        </div>
      </div>

      <div class="action-card validate-card">
        <div class="action-card-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
        </div>
        <div class="action-card-info">
          <div class="action-card-title">完整性验证</div>
          <div class="action-card-desc">验证区块链数据是否被篡改</div>
        </div>
        <el-button size="large" @click="validateChain" :loading="validating" class="btn-validate">
          验证链完整性
        </el-button>
      </div>
    </div>

    <!-- 验证结果 -->
    <div v-if="validationResult !== null" class="validate-result" :class="validationResult ? 'success' : 'fail'">
      <div class="vr-icon">
        <svg v-if="validationResult" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
        <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
      </div>
      <strong>{{ validationResult ? '区块链数据完整，所有区块有效' : '警告：区块链数据已被篡改！' }}</strong>
    </div>

    <!-- 链数据 -->
    <div class="chain-section">
      <div class="section-header">
        <h3>区块链账本</h3>
        <span class="section-badge">{{ chain.length }} 个区块</span>
      </div>

      <div v-if="chain.length === 0" class="empty-state">
        <div class="empty-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="48" height="48"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M8 12h8M12 8v8"/></svg>
        </div>
        <h4>区块链暂无数据</h4>
        <p>请先在接收方页面验证发票并保存，然后在此处输入发票ID完成上链</p>
      </div>

      <div v-else class="chain-blocks">
        <div v-for="(block, i) in chain" :key="block.id" class="block-wrapper">
          <!-- 连接线 -->
          <div v-if="i < chain.length - 1" class="block-connector">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><polyline points="6 9 12 15 18 9"/></svg>
          </div>

          <div class="block-card" :class="{ 'genesis': block.blockIndex === 1 }">
            <div class="block-head">
              <div class="block-index">
                <span class="block-num">#{{ block.blockIndex }}</span>
                <span class="block-tag" :class="block.blockIndex === 1 ? 'gen' : 'data'">
                  {{ block.blockIndex === 1 ? '创世' : '数据' }}
                </span>
              </div>
              <span class="block-time">{{ formatTime(block.timestamp) }}</span>
            </div>

            <div class="block-body">
              <div class="block-field">
                <span class="field-label">区块 Hash</span>
                <span class="field-value hash">{{ block.hash }}</span>
              </div>
              <div class="block-field">
                <span class="field-label">前驱 Hash</span>
                <span class="field-value hash dim">{{ block.previousHash }}</span>
              </div>
              <div class="block-meta">
                <span class="meta-item">Nonce: <strong>{{ block.nonce }}</strong></span>
                <span class="meta-item">关联发票: <strong>#{{ block.invoiceId }}</strong></span>
              </div>
            </div>

            <div class="block-foot">
              <el-button size="small" @click="viewBlockDetail(block.id)" class="btn-detail">
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 区块详情弹窗 -->
    <el-dialog v-model="blockDetailVisible" title="区块详情" width="680px" destroy-on-close>
      <div v-if="blockDetail" class="block-detail">
        <h4 class="detail-section-title">区块信息</h4>
        <div class="detail-grid">
          <div class="d-item"><span class="d-label">区块索引</span><span class="d-value">{{ blockDetail.block?.blockIndex }}</span></div>
          <div class="d-item"><span class="d-label">Nonce</span><span class="d-value">{{ blockDetail.block?.nonce }}</span></div>
          <div class="d-item"><span class="d-label">时间戳</span><span class="d-value">{{ blockDetail.block?.timestamp }}</span></div>
          <div class="d-item"><span class="d-label">创建时间</span><span class="d-value">{{ blockDetail.block?.createTime }}</span></div>
          <div class="d-item full"><span class="d-label">区块 Hash</span><span class="d-value hash">{{ blockDetail.block?.hash }}</span></div>
          <div class="d-item full"><span class="d-label">前驱 Hash</span><span class="d-value hash dim">{{ blockDetail.block?.previousHash }}</span></div>
        </div>

        <h4 v-if="blockDetail.invoice" class="detail-section-title">关联发票</h4>
        <div v-if="blockDetail.invoice" class="detail-grid">
          <div class="d-item"><span class="d-label">发票号码</span><span class="d-value">{{ blockDetail.invoice.invoiceNumber }}</span></div>
          <div class="d-item"><span class="d-label">发票代码</span><span class="d-value">{{ blockDetail.invoice.invoiceCode }}</span></div>
          <div class="d-item"><span class="d-label">销售方</span><span class="d-value">{{ blockDetail.invoice.sellerName }}</span></div>
          <div class="d-item"><span class="d-label">购买方</span><span class="d-value">{{ blockDetail.invoice.buyerName }}</span></div>
          <div class="d-item"><span class="d-label">价税合计</span><span class="d-value fw-bold">{{ formatMoney(blockDetail.invoice.totalAmount) }}</span></div>
          <div class="d-item"><span class="d-label">发票类型</span><span class="d-value">{{ blockDetail.invoice.invoiceType }}</span></div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const chain = ref([])
const adding = ref(false)
const chainInvoiceId = ref(null)
const validating = ref(false)
const validationResult = ref(null)
const blockDetailVisible = ref(false)
const blockDetail = ref(null)

onMounted(() => loadChain())

async function loadChain() {
  try {
    const res = await api.getChain()
    chain.value = Array.isArray(res.data.data) ? res.data.data : (Array.isArray(res.data) ? res.data : [])
  } catch (e) {
    const msg = e.response?.data?.message || e.response?.statusText || e.message
    ElMessage.error('加载链数据失败: ' + msg)
    console.error('Chain load error:', e)
  }
}

async function addToChain() {
  adding.value = true
  try {
    const invRes = await api.listInvoices()
    const invoices = invRes.data.data || []
    const inv = invoices.find(i => i.id === chainInvoiceId.value)
    if (!inv) { ElMessage.error('未找到该发票'); return }

    await api.addBlock(chainInvoiceId.value, {
      invoiceNumber: inv.invoiceNumber, invoiceCode: inv.invoiceCode,
      sellerName: inv.sellerName, buyerName: inv.buyerName,
      totalAmount: inv.totalAmount, invoiceType: inv.invoiceType
    })
    ElMessage.success('发票已成功上链'); loadChain()
  } catch (e) { ElMessage.error('上链失败: ' + (e.response?.data?.message || e.message)) }
  finally { adding.value = false }
}

async function validateChain() {
  validating.value = true
  try {
    const res = await api.validateChain()
    validationResult.value = res.data.data
    ElMessage[validationResult.value ? 'success' : 'error'](res.data.message)
  } catch { ElMessage.error('验证请求失败') }
  finally { validating.value = false }
}

async function viewBlockDetail(id) {
  try {
    const res = await api.getBlockDetail(id)
    blockDetail.value = res.data.data; blockDetailVisible.value = true
  } catch { ElMessage.error('获取详情失败') }
}

function formatTime(ts) {
  if (!ts) return '-'
  return new Date(ts).toLocaleString('zh-CN', { hour12: false })
}
function formatMoney(val) {
  if (val === null || val === undefined) return '-'
  return '¥' + Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}
</script>

<style scoped>
.blockchain-page { width: 100%; }

.page-hero { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
.hero-icon {
  width: 48px; height: 48px; border-radius: 12px;
  background: linear-gradient(135deg, #8B5CF6, #7C3AED);
  display: flex; align-items: center; justify-content: center; color: #fff; flex-shrink: 0;
}
.hero-icon svg { width: 24px; height: 24px; }
.page-hero h2 { font-size: 20px; font-weight: 700; color: #1E293B; }
.page-hero p { font-size: 13px; color: #64748B; margin-top: 2px; }

.top-actions { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 20px; }
.action-card {
  background: #fff; border-radius: 12px; padding: 20px 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,.04); display: flex; align-items: center; gap: 16px;
}
.action-card-icon {
  width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.mint-card .action-card-icon { background: #EEF1FE; color: #4F6EF7; }
.validate-card .action-card-icon { background: #ECFDF5; color: #059669; }
.action-card-info { flex: 1; }
.action-card-title { font-size: 15px; font-weight: 700; }
.action-card-desc { font-size: 11px; color: #94A3B8; margin-top: 2px; }
.action-card-form { display: flex; gap: 10px; align-items: center; }
.btn-primary { background: linear-gradient(135deg, #4F6EF7, #6B7FF7); border: none; border-radius: 8px; font-weight: 600; }
.btn-validate { border: 1px solid #A7F3D0; background: #ECFDF5; color: #059669; font-weight: 600; border-radius: 8px; }
.btn-validate:hover { background: #D1FAE5; }

.validate-result {
  display: flex; align-items: center; gap: 12px; padding: 14px 20px; border-radius: 10px; margin-bottom: 20px;
}
.validate-result.success { background: #ECFDF5; border: 1px solid #A7F3D0; color: #065F46; }
.validate-result.fail { background: #FEF2F2; border: 1px solid #FECACA; color: #991B1B; }
.vr-icon svg { width: 22px; height: 22px; }

.chain-section { background: #fff; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,.04); padding: 20px 24px; }
.section-header { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; }
.section-header h3 { font-size: 16px; font-weight: 700; }
.section-badge { padding: 2px 10px; border-radius: 12px; font-size: 11px; font-weight: 600; background: #F1F5F9; color: #64748B; }

.empty-state { text-align: center; padding: 48px 20px; }
.empty-icon { color: #CBD5E1; margin-bottom: 16px; }
.empty-state h4 { font-size: 15px; color: #64748B; margin-bottom: 4px; }
.empty-state p { font-size: 12px; color: #94A3B8; }

.chain-blocks { position: relative; }
.block-wrapper { position: relative; }
.block-connector {
  display: flex; justify-content: center; padding: 4px 0;
  color: #CBD5E1;
}

.block-card {
  border: 1px solid #E2E8F0; border-radius: 10px; padding: 18px 20px;
  background: #fff; transition: all .2s;
}
.block-card:hover { border-color: #CBD5E1; box-shadow: 0 2px 8px rgba(0,0,0,.04); }
.block-card.genesis { border-color: #DDD6FE; background: #FAFAFE; }

.block-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.block-index { display: flex; align-items: center; gap: 8px; }
.block-num { font-size: 15px; font-weight: 700; color: #1E293B; font-family: 'JetBrains Mono', monospace; }
.block-tag { padding: 1px 8px; border-radius: 10px; font-size: 10px; font-weight: 600; }
.block-tag.gen { background: #EDE9FE; color: #7C3AED; }
.block-tag.data { background: #EEF1FE; color: #4F6EF7; }
.block-time { font-size: 11px; color: #94A3B8; font-family: monospace; }

.block-field { margin-bottom: 10px; }
.field-label { display: block; font-size: 10px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; color: #94A3B8; margin-bottom: 3px; }
.field-value { font-family: 'JetBrains Mono', monospace; font-size: 11px; word-break: break-all; display: block; }
.field-value.hash { color: #4F6EF7; }
.field-value.hash.dim { color: #94A3B8; }

.block-meta { display: flex; gap: 20px; }
.meta-item { font-size: 11px; color: #64748B; }
.meta-item strong { color: #1E293B; font-family: monospace; }

.block-foot { margin-top: 12px; text-align: right; }
.btn-detail { border: 1px solid #E2E8F0; border-radius: 6px; font-size: 12px; }

.detail-section-title { font-size: 14px; font-weight: 700; margin: 16px 0 10px; }
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1px; background: #E2E8F0; border-radius: 8px; overflow: hidden; }
.d-item { background: #fff; padding: 12px 16px; }
.d-item.full { grid-column: span 2; }
.d-label { font-size: 11px; color: #94A3B8; display: block; margin-bottom: 2px; }
.d-value { font-size: 13px; font-weight: 600; color: #1E293B; word-break: break-all; }
.d-value.hash { font-family: 'JetBrains Mono', monospace; font-size: 11px; color: #4F6EF7; }
.d-value.hash.dim { color: #94A3B8; }
.fw-bold { font-weight: 700; }
</style>
