<template>
  <div class="receiver-page">
    <div class="page-hero">
      <div class="hero-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5"/><path d="M12 5l-7 7 7 7"/></svg>
      </div>
      <div>
        <h2>接收方（乙）— 解密与验证签名</h2>
        <p>接收发送方的密文和数字签名，解密还原 XML 并通过数字指纹比对验证完整性</p>
      </div>
    </div>

    <!-- 流水线 -->
    <div class="pipeline">
      <div v-for="(step, i) in steps" :key="i" class="pipe-node"
        :class="{ 'pipe--done': activeStep > i, 'pipe--active': activeStep === i }">
        <div class="pipe-dot">
          <span v-if="activeStep > i" class="check-mark">&#10003;</span>
          <span v-else class="dot-num">{{ i + 1 }}</span>
        </div>
        <div class="pipe-info">
          <span class="pipe-title">{{ step.title }}</span>
          <span class="pipe-desc">{{ step.desc }}</span>
        </div>
        <div v-if="i < steps.length - 1" class="pipe-line" :class="{ 'line--filled': activeStep > i }"></div>
      </div>
    </div>

    <!-- 步骤1：输入密文 -->
    <div v-show="activeStep === 0" class="step-panel">
      <div class="panel-header">
        <span class="panel-badge">步骤 1</span>
        <h3>输入接收到的密文和数字签名</h3>
      </div>
      <div class="panel-body">
        <div v-if="hasSenderData" class="from-sender-bar">
          <div class="sender-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
          </div>
          <div class="sender-info">
            <strong>检测到发送方传递的数据</strong>
            <span>点击按钮自动填入完整密文和数字签名，无需手动粘贴</span>
          </div>
          <el-button type="success" @click="loadSenderData">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/></svg>
            自动加载
          </el-button>
        </div>

        <div class="input-group">
          <label class="input-label">
            <span class="label-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
            </span>
            传输密文（AES 加密）
          </label>
          <textarea v-model="inputCipherText" class="code-input" rows="6"
            placeholder="在此粘贴从发送方接收到的密文..."></textarea>
        </div>
        <div class="input-group">
          <label class="input-label">
            <span class="label-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>
            </span>
            数字签名（RSA 签名）
          </label>
          <textarea v-model="inputSignature" class="code-input" rows="4"
            placeholder="在此粘贴从发送方接收到的数字签名..."></textarea>
        </div>

        <div class="action-bar">
          <el-button size="large" type="primary" @click="step1Decrypt" :disabled="!inputCipherText || !inputSignature" class="btn-primary">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
            解密并验证签名
          </el-button>
        </div>
      </div>
    </div>

    <!-- 步骤2：解密 -->
    <div v-show="activeStep === 1" class="step-panel">
      <div class="panel-header">
        <span class="panel-badge active">步骤 2</span>
        <h3>AES 对称解密 — 还原明文 XML</h3>
      </div>
      <div class="panel-body">
        <div class="info-banner">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4M12 8h.01"/></svg>
          <span>使用 AES 对称密钥解密，还原出原始 XML 文档和数字签名</span>
        </div>
        <div class="result-card decrypted-card">
          <div class="result-card-label">解密后的明文 XML</div>
          <div class="result-card-value">{{ decryptedXml }}</div>
        </div>
        <div class="action-bar">
          <el-button size="large" @click="activeStep = 0" class="btn-ghost">上一步</el-button>
          <el-button size="large" type="primary" @click="activeStep = 2" class="btn-primary">
            验证签名
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 步骤3：验证 -->
    <div v-show="activeStep === 2" class="step-panel">
      <div class="panel-header">
        <span class="panel-badge" :class="verifyResult?.success ? 'done' : 'error'">步骤 3</span>
        <h3>数字签名验证结果</h3>
      </div>
      <div class="panel-body">
        <div class="verify-result" :class="verifyResult?.success ? 'verify-ok' : 'verify-fail'">
          <div class="verify-icon">
            <svg v-if="verifyResult?.success" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
          </div>
          <div>
            <div class="verify-title">{{ verifyResult?.message }}</div>
            <div v-if="verifyResult" class="verify-meta">
              <span>数字指纹: {{ verifyResult.digitalFingerprint?.substring(0, 16) }}...</span>
            </div>
          </div>
        </div>

        <div class="action-bar">
          <el-button size="large" @click="activeStep = 1" class="btn-ghost">上一步</el-button>
          <el-button size="large" type="primary" @click="activeStep = 3" :disabled="!verifyResult?.success" class="btn-primary">
            查看发票信息
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 步骤4：发票信息 -->
    <div v-show="activeStep === 3" class="step-panel">
      <div class="panel-header">
        <span class="panel-badge done">验证通过</span>
        <h3>发票详细信息</h3>
      </div>
      <div class="panel-body">
        <div class="info-banner success">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
          <span>签名验证通过 — 电子发票完整性已确认，发送者身份已认证</span>
        </div>

        <div v-if="verifyResult?.invoiceData" class="invoice-grid">
          <div class="inv-item" v-for="item in invoiceFields" :key="item.key">
            <div class="inv-label">{{ item.label }}</div>
            <div class="inv-value">{{ verifyResult.invoiceData[item.key] || '-' }}</div>
          </div>
        </div>

        <div class="action-bar">
          <el-button size="large" @click="activeStep = 2" class="btn-ghost">上一步</el-button>
          <el-button size="large" @click="reset" class="btn-ghost">重新验证</el-button>
          <el-button size="large" type="primary" @click="saveToDb" :loading="saving" class="btn-primary">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/><polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/></svg>
            保存到数据库
          </el-button>
          <el-button size="large" type="success" @click="goToBlockchain" :disabled="!savedInvoiceId" class="btn-send">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M8 12h8M12 8v8"/></svg>
            区块上链
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'

const router = useRouter()

const steps = [
  { title: '输入密文', desc: '接收传输数据' },
  { title: '对称解密', desc: 'AES 解密还原' },
  { title: '验证签名', desc: '指纹比对认证' },
  { title: '查看结果', desc: '发票信息确认' },
]

const activeStep = ref(0)
const inputCipherText = ref('')
const inputSignature = ref('')
const decryptedXml = ref('')
const verifyResult = ref(null)
const saving = ref(false)
const savedInvoiceId = ref(null)

const invoiceFields = [
  { key: 'invoiceNumber', label: '发票号码' },
  { key: 'invoiceCode', label: '发票代码' },
  { key: 'sellerName', label: '销售方名称' },
  { key: 'sellerTaxNo', label: '销售方税号' },
  { key: 'buyerName', label: '购买方名称' },
  { key: 'buyerTaxNo', label: '购买方税号' },
  { key: 'amount', label: '金额' },
  { key: 'taxAmount', label: '税额' },
  { key: 'totalAmount', label: '价税合计' },
  { key: 'invoiceDate', label: '开票日期' },
  { key: 'invoiceType', label: '发票类型' },
  { key: 'remark', label: '备注' },
]

const hasSenderData = computed(() => !!(sessionStorage.getItem('sender_cipherText') && sessionStorage.getItem('sender_signature')))

function loadSenderData() {
  const ct = sessionStorage.getItem('sender_cipherText')
  const sig = sessionStorage.getItem('sender_signature')
  if (ct && sig) {
    inputCipherText.value = ct; inputSignature.value = sig
    ElMessage.success('已从发送方加载完整数据')
  } else { ElMessage.warning('未找到发送方数据') }
}

async function step1Decrypt() {
  try {
    const dr = await api.decrypt(inputCipherText.value)
    decryptedXml.value = dr.data.data?.plainText || ''
    activeStep.value = 1; ElMessage.success('AES 解密成功')

    const vr = await api.verify(inputCipherText.value, inputSignature.value)
    verifyResult.value = vr.data.data; activeStep.value = 2
    ElMessage[vr.data.data?.success ? 'success' : 'error'](vr.data.data?.message)
  } catch (e) { ElMessage.error('操作失败: ' + (e.response?.data?.message || e.message)) }
}

async function saveToDb() {
  saving.value = true
  try {
    const res = await api.saveInvoice({
      invoiceData: verifyResult.value.invoiceData,
      xmlContent: decryptedXml.value,
      digitalFingerprint: verifyResult.value.digitalFingerprint,
      digitalSignature: inputSignature.value
    })
    savedInvoiceId.value = res.data.data
    ElMessage.success('发票已存入数据库，ID: ' + savedInvoiceId.value)
  } catch (e) { ElMessage.error('保存失败: ' + (e.response?.data?.message || e.message)) }
  finally { saving.value = false }
}

function goToBlockchain() { router.push('/blockchain') }
function reset() { activeStep.value = 0; decryptedXml.value = ''; verifyResult.value = null; savedInvoiceId.value = null }
</script>

<style scoped>
.receiver-page { width: 100%; }

.page-hero { display: flex; align-items: center; gap: 16px; margin-bottom: 28px; }
.hero-icon {
  width: 48px; height: 48px; border-radius: 12px;
  background: linear-gradient(135deg, #22C55E, #059669);
  display: flex; align-items: center; justify-content: center; color: #fff; flex-shrink: 0;
}
.hero-icon svg { width: 24px; height: 24px; }
.page-hero h2 { font-size: 20px; font-weight: 700; color: #1E293B; }
.page-hero p { font-size: 13px; color: #64748B; margin-top: 2px; }

/* Pipeline */
.pipeline { display: flex; align-items: flex-start; padding: 20px 24px; background: #fff; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,.04); margin-bottom: 24px; position: relative; }
.pipe-node { display: flex; align-items: center; gap: 10px; flex: 1; position: relative; }
.pipe-dot {
  width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700; flex-shrink: 0; z-index: 1;
  background: #F1F5F9; color: #94A3B8; transition: all .3s;
}
.pipe--active .pipe-dot { background: #4F6EF7; color: #fff; box-shadow: 0 0 0 4px rgba(79,110,247,.15); }
.pipe--done .pipe-dot { background: #22C55E; color: #fff; }
.check-mark { font-size: 14px; }
.pipe-info { display: flex; flex-direction: column; }
.pipe-title { font-size: 13px; font-weight: 600; color: #94A3B8; }
.pipe--active .pipe-title, .pipe--done .pipe-title { color: #1E293B; }
.pipe-desc { font-size: 11px; color: #CBD5E1; }
.pipe--active .pipe-desc { color: #64748B; }
.pipe-line {
  position: absolute; left: 36px; top: 16px; width: calc(100% - 44px); height: 2px;
  background: #F1F5F9; z-index: 0;
}
.pipe-line::after {
  content: ''; position: absolute; left: 0; top: 0; height: 100%;
  background: #22C55E; transition: width .4s;
}
.line--filled::after { width: 100%; }

/* Steps */
.step-panel { background: #fff; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,.04); overflow: hidden; }
.panel-header { padding: 20px 24px 0; display: flex; align-items: center; gap: 12px; }
.panel-badge { padding: 4px 10px; border-radius: 20px; font-size: 11px; font-weight: 600; background: #F1F5F9; color: #64748B; }
.panel-badge.active { background: #EEF1FE; color: #4F6EF7; }
.panel-badge.done { background: #ECFDF5; color: #059669; }
.panel-badge.error { background: #FEF2F2; color: #DC2626; }
.panel-header h3 { font-size: 16px; font-weight: 600; }
.panel-body { padding: 16px 24px 24px; }

.from-sender-bar {
  display: flex; align-items: center; gap: 14px;
  padding: 14px 18px; background: linear-gradient(135deg, #ECFDF5, #F0FDF4);
  border: 1px solid #A7F3D0; border-radius: 10px; margin-bottom: 20px;
}
.sender-icon { color: #059669; flex-shrink: 0; }
.sender-info { flex: 1; }
.sender-info strong { display: block; font-size: 13px; color: #065F46; }
.sender-info span { font-size: 11px; color: #047857; }

.input-group { margin-bottom: 16px; }
.input-label { display: flex; align-items: center; gap: 6px; font-size: 13px; font-weight: 600; margin-bottom: 8px; color: #475569; }
.label-icon { color: #94A3B8; }
.code-input {
  width: 100%; padding: 12px 16px; border: 1px solid #E2E8F0; border-radius: 8px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace; font-size: 12px; line-height: 1.6;
  resize: vertical; outline: none; transition: border-color .2s; color: #334155;
}
.code-input:focus { border-color: #4F6EF7; box-shadow: 0 0 0 3px rgba(79,110,247,.1); }
.code-input::placeholder { color: #CBD5E1; }

.info-banner { display: flex; align-items: center; gap: 10px; padding: 12px 16px; background: #EEF1FE; border-radius: 8px; font-size: 13px; color: #4F6EF7; margin-bottom: 20px; }
.info-banner.success { background: #ECFDF5; color: #059669; }

.result-card { padding: 16px 20px; border-radius: 10px; margin-bottom: 20px; }
.result-card-label { font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 8px; }
.result-card-value { font-family: 'JetBrains Mono', 'Fira Code', monospace; font-size: 12px; word-break: break-all; line-height: 1.6; }
.decrypted-card { background: #F0F9FF; border: 1px solid #BAE6FD; }
.decrypted-card .result-card-label { color: #0369A1; }
.decrypted-card .result-card-value { color: #0C4A6E; }

.verify-result { display: flex; align-items: center; gap: 16px; padding: 20px 24px; border-radius: 12px; margin-bottom: 20px; }
.verify-ok { background: #ECFDF5; border: 1px solid #A7F3D0; }
.verify-fail { background: #FEF2F2; border: 1px solid #FECACA; }
.verify-icon { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.verify-ok .verify-icon { background: #D1FAE5; color: #059669; }
.verify-fail .verify-icon { background: #FEE2E2; color: #DC2626; }
.verify-icon svg { width: 24px; height: 24px; }
.verify-title { font-size: 15px; font-weight: 600; }
.verify-ok .verify-title { color: #065F46; }
.verify-fail .verify-title { color: #991B1B; }
.verify-meta { font-size: 11px; color: #64748B; margin-top: 4px; font-family: monospace; }

.invoice-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1px; background: #E2E8F0; border-radius: 10px; overflow: hidden; margin-bottom: 24px; }
.inv-item { background: #fff; padding: 14px 18px; }
.inv-label { font-size: 11px; color: #94A3B8; font-weight: 500; margin-bottom: 4px; }
.inv-value { font-size: 13px; font-weight: 600; color: #1E293B; word-break: break-all; }

.action-bar { display: flex; gap: 10px; flex-wrap: wrap; margin-top: 20px; }
.btn-primary {
  background: linear-gradient(135deg, #4F6EF7, #6B7FF7); border: none; border-radius: 8px;
  display: inline-flex; align-items: center; gap: 6px; font-weight: 600; padding: 10px 24px; color: #fff;
}
.btn-primary:hover { background: linear-gradient(135deg, #3B54D4, #5A6FE8); }
.btn-send {
  background: linear-gradient(135deg, #22C55E, #16A34A); border: none; border-radius: 8px;
  display: inline-flex; align-items: center; gap: 6px; font-weight: 600; padding: 10px 24px; color: #fff;
}
.btn-send:hover { background: linear-gradient(135deg, #16A34A, #15803D); }
.btn-ghost { border: none; background: transparent; color: #64748B; font-weight: 600; border-radius: 8px; padding: 10px 24px; }
.btn-ghost:hover { background: #F1F5F9; }
</style>
