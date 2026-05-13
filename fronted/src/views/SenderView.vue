<template>
  <div class="sender-page">
    <div class="page-hero">
      <div class="hero-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 19l7-7-7-7"/><path d="M5 12h14"/></svg>
      </div>
      <div>
        <h2>发送方（甲）— 电子发票签名与加密</h2>
        <p>准备XML发票数据，依次生成数字指纹、数字签名，最终通过对称加密安全传输</p>
      </div>
    </div>

    <!-- 流水线进度条 -->
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

    <!-- 步骤1：输入XML -->
    <div v-show="activeStep === 0" class="step-panel">
      <div class="panel-header">
        <span class="panel-badge">步骤 1</span>
        <h3>输入或粘贴发票 XML 内容</h3>
      </div>
      <div class="panel-body">
        <el-input v-model="xmlContent" type="textarea" :rows="14"
          placeholder="在此粘贴电子发票的 XML 内容..."
          class="xml-input" />
        <div class="action-bar">
          <el-button size="large" @click="loadSample" class="btn-outline">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M9 3v18"/></svg>
            加载示例 XML
          </el-button>
          <el-button size="large" type="primary" @click="step1Generate" :disabled="!xmlContent" class="btn-primary">
            生成数字指纹
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 步骤2：数字指纹 -->
    <div v-show="activeStep === 1" class="step-panel">
      <div class="panel-header">
        <span class="panel-badge active">步骤 2</span>
        <h3>SHA-256 生成数字指纹</h3>
      </div>
      <div class="panel-body">
        <div class="info-banner">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4M12 8h.01"/></svg>
          <span>对明文 XML 使用 SHA-256 单向散列算法生成数字指纹（摘要）</span>
        </div>
        <div class="result-card fingerprint-card">
          <div class="result-card-label">数字指纹</div>
          <div class="result-card-value">{{ digitalFingerprint }}</div>
        </div>
        <div class="action-bar">
          <el-button size="large" @click="activeStep = 0" class="btn-ghost">上一步</el-button>
          <el-button size="large" type="primary" @click="step2Generate" class="btn-primary">
            生成数字签名
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 步骤3：数字签名 -->
    <div v-show="activeStep === 2" class="step-panel">
      <div class="panel-header">
        <span class="panel-badge active">步骤 3</span>
        <h3>RSA 非对称加密生成数字签名</h3>
      </div>
      <div class="panel-body">
        <div class="info-banner">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4M12 8h.01"/></svg>
          <span>使用 RSA 私钥对数字指纹加密，形成数字签名</span>
        </div>
        <div class="result-card signature-card">
          <div class="result-card-label">数字签名（Base64）</div>
          <div class="result-card-value">{{ digitalSignature }}</div>
        </div>
        <div class="action-bar">
          <el-button size="large" @click="activeStep = 1" class="btn-ghost">上一步</el-button>
          <el-button size="large" type="primary" @click="step3Encrypt" class="btn-primary">
            AES 对称加密传输
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 步骤4：发送 -->
    <div v-show="activeStep === 3" class="step-panel">
      <div class="panel-header">
        <span class="panel-badge done">完成</span>
        <h3>传输数据已就绪</h3>
      </div>
      <div class="panel-body">
        <div class="info-banner success">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
          <span>发送方处理完成！以下是需要传输给接收方的完整数据</span>
        </div>

        <div class="send-data-grid">
          <div class="data-card">
            <div class="data-card-head">
              <span class="data-card-icon fp">FP</span>
              <div>
                <div class="data-card-title">数字指纹</div>
                <div class="data-card-algo">SHA-256</div>
              </div>
              <el-button size="small" text @click="copy(digitalFingerprint)">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>
              </el-button>
            </div>
            <div class="data-card-body fp-value">{{ digitalFingerprint }}</div>
          </div>

          <div class="data-card highlight">
            <div class="data-card-head">
              <span class="data-card-icon sig">DS</span>
              <div>
                <div class="data-card-title">数字签名</div>
                <div class="data-card-algo">RSA 私钥加密 → 发送给接收方</div>
              </div>
              <el-button size="small" type="warning" text @click="copy(digitalSignature)">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>
              </el-button>
            </div>
            <div class="data-card-body sig-value">{{ digitalSignature }}</div>
          </div>

          <div class="data-card highlight">
            <div class="data-card-head">
              <span class="data-card-icon enc">EN</span>
              <div>
                <div class="data-card-title">传输密文</div>
                <div class="data-card-algo">AES 对称加密 → 发送给接收方</div>
              </div>
              <el-button size="small" type="danger" text @click="copy(cipherText)">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>
              </el-button>
            </div>
            <div class="data-card-body enc-value">{{ cipherText }}</div>
          </div>
        </div>

        <div class="action-bar">
          <el-button size="large" @click="activeStep = 2" class="btn-ghost">上一步</el-button>
          <el-button size="large" @click="reset" class="btn-ghost">重新开始</el-button>
          <el-button size="large" @click="copyAll" class="btn-outline">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>
            一键复制全部
          </el-button>
          <el-button size="large" type="primary" @click="goToReceiver" class="btn-primary btn-send">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
            发送并跳转验证
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'

const router = useRouter()

const steps = [
  { title: '输入 XML', desc: '准备发票数据' },
  { title: '数字指纹', desc: 'SHA-256 摘要' },
  { title: '数字签名', desc: 'RSA 非对称加密' },
  { title: '加密传输', desc: 'AES 对称加密' },
]

const activeStep = ref(0)
const xmlContent = ref('')
const digitalFingerprint = ref('')
const digitalSignature = ref('')
const cipherText = ref('')

const sampleXml = `<?xml version="1.0" encoding="UTF-8"?>
<invoice>
    <invoiceNumber>INV20240513001</invoiceNumber>
    <invoiceCode>044001900111</invoiceCode>
    <sellerName>深圳市科技有限公司</sellerName>
    <sellerTaxNo>91440300MA5ABCD123</sellerTaxNo>
    <buyerName>北京贸易有限公司</buyerName>
    <buyerTaxNo>91110108MA5EFGH456</buyerTaxNo>
    <amount>10000.00</amount>
    <taxAmount>1300.00</taxAmount>
    <totalAmount>11300.00</totalAmount>
    <invoiceDate>2024-05-13 10:30:00</invoiceDate>
    <invoiceType>增值税电子发票</invoiceType>
    <remark>办公设备采购</remark>
</invoice>`

function loadSample() { xmlContent.value = sampleXml; ElMessage.success('示例 XML 已加载') }

async function step1Generate() {
  try {
    const res = await api.generateFingerprint(xmlContent.value)
    digitalFingerprint.value = res.data.data; activeStep.value = 1
    ElMessage.success('数字指纹生成成功')
  } catch (e) { ElMessage.error('生成失败: ' + (e.response?.data?.message || e.message)) }
}

async function step2Generate() {
  try {
    const res = await api.generateSignature(digitalFingerprint.value)
    digitalSignature.value = res.data.data; activeStep.value = 2
    ElMessage.success('数字签名生成成功')
  } catch (e) { ElMessage.error('生成失败: ' + (e.response?.data?.message || e.message)) }
}

async function step3Encrypt() {
  try {
    const res = await api.encrypt(xmlContent.value, digitalSignature.value)
    cipherText.value = res.data.data; activeStep.value = 3
    ElMessage.success('AES 加密完成，数据已就绪')
  } catch (e) { ElMessage.error('加密失败: ' + (e.response?.data?.message || e.message)) }
}

async function copy(text) {
  try { await navigator.clipboard.writeText(text); ElMessage.success('已复制') }
  catch {
    const ta = document.createElement('textarea'); ta.value = text
    document.body.appendChild(ta); ta.select(); document.execCommand('copy')
    document.body.removeChild(ta); ElMessage.success('已复制')
  }
}
function copyAll() { copy(`数字签名:\n${digitalSignature.value}\n\n传输密文:\n${cipherText.value}`) }

function goToReceiver() {
  sessionStorage.setItem('sender_cipherText', cipherText.value)
  sessionStorage.setItem('sender_signature', digitalSignature.value)
  sessionStorage.setItem('sender_fingerprint', digitalFingerprint.value)
  router.push('/receiver')
}

function reset() { activeStep.value = 0; digitalFingerprint.value = ''; digitalSignature.value = ''; cipherText.value = '' }
</script>

<style scoped>
.sender-page { width: 100%; }

.page-hero { display: flex; align-items: center; gap: 16px; margin-bottom: 28px; }
.hero-icon {
  width: 48px; height: 48px; border-radius: 12px;
  background: linear-gradient(135deg, #4F6EF7, #7C5CFC);
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

/* Step panel */
.step-panel { background: #fff; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,.04); overflow: hidden; }
.panel-header { padding: 20px 24px 0; display: flex; align-items: center; gap: 12px; }
.panel-badge {
  padding: 4px 10px; border-radius: 20px; font-size: 11px; font-weight: 600;
  background: #F1F5F9; color: #64748B;
}
.panel-badge.active { background: #EEF1FE; color: #4F6EF7; }
.panel-badge.done { background: #ECFDF5; color: #059669; }
.panel-header h3 { font-size: 16px; font-weight: 600; }
.panel-body { padding: 16px 24px 24px; }

.xml-input :deep(textarea) { font-family: 'JetBrains Mono', 'Fira Code', monospace; font-size: 13px; line-height: 1.6; border-radius: 8px; }

.info-banner {
  display: flex; align-items: center; gap: 10px; padding: 12px 16px;
  background: #EEF1FE; border-radius: 8px; font-size: 13px; color: #4F6EF7; margin-bottom: 20px;
}
.info-banner.success { background: #ECFDF5; color: #059669; }

.result-card {
  padding: 16px 20px; border-radius: 10px; margin-bottom: 20px;
}
.result-card-label { font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 8px; }
.result-card-value { font-family: 'JetBrains Mono', 'Fira Code', monospace; font-size: 12px; word-break: break-all; line-height: 1.5; }
.fingerprint-card { background: #F0F9FF; border: 1px solid #BAE6FD; }
.fingerprint-card .result-card-label { color: #0369A1; }
.fingerprint-card .result-card-value { color: #0C4A6E; }
.signature-card { background: #FFFBEB; border: 1px solid #FDE68A; }
.signature-card .result-card-label { color: #B45309; }
.signature-card .result-card-value { color: #78350F; }

.send-data-grid { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 16px; margin-bottom: 24px; }
.data-card {
  background: #fff; border: 1px solid #E2E8F0; border-radius: 10px; padding: 16px 20px;
  transition: all .2s;
}
.data-card.highlight { border-color: #FDE68A; background: #FFFDF5; }
.data-card-head { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; }
.data-card-icon {
  width: 36px; height: 36px; border-radius: 8px; display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 700; flex-shrink: 0;
}
.data-card-icon.fp { background: #F0F9FF; color: #0369A1; }
.data-card-icon.sig { background: #FFFBEB; color: #B45309; }
.data-card-icon.enc { background: #FEF2F2; color: #DC2626; }
.data-card-title { font-size: 13px; font-weight: 600; }
.data-card-algo { font-size: 11px; color: #94A3B8; }
.data-card-body { font-family: 'JetBrains Mono', 'Fira Code', monospace; font-size: 12px; word-break: break-all; line-height: 1.6; }
.fp-value { color: #0C4A6E; }
.sig-value { color: #78350F; }
.enc-value { color: #991B1B; }

.action-bar { display: flex; gap: 10px; flex-wrap: wrap; margin-top: 20px; }
.btn-primary {
  background: linear-gradient(135deg, #4F6EF7, #6B7FF7); border: none; border-radius: 8px;
  display: inline-flex; align-items: center; gap: 6px; font-weight: 600; padding: 10px 24px;
}
.btn-primary:hover { background: linear-gradient(135deg, #3B54D4, #5A6FE8); }
.btn-send { background: linear-gradient(135deg, #22C55E, #16A34A); }
.btn-send:hover { background: linear-gradient(135deg, #16A34A, #15803D); }
.btn-outline {
  border: 1px solid #E2E8F0; border-radius: 8px; background: #fff; display: inline-flex; align-items: center; gap: 6px;
  font-weight: 600; padding: 10px 24px;
}
.btn-outline:hover { border-color: #CBD5E1; background: #F8FAFC; }
.btn-ghost { border: none; background: transparent; color: #64748B; font-weight: 600; border-radius: 8px; padding: 10px 24px; }
.btn-ghost:hover { background: #F1F5F9; }
</style>
