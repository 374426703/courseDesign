<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="logo-area">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="3" width="18" height="18" rx="3"/>
            <path d="M9 12h6M12 9v6"/>
            <circle cx="12" cy="12" r="1.5" fill="currentColor" stroke="none"/>
          </svg>
        </div>
        <div class="logo-text">
          <span class="logo-title">电子发票验签系统</span>
          <span class="logo-sub">Digital Signature System</span>
        </div>
      </div>

      <nav class="nav-menu">
        <router-link to="/sender" class="nav-item" active-class="nav-item--active">
          <span class="nav-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 19l7-7-7-7"/><path d="M5 12h14"/></svg>
          </span>
          <span class="nav-label">
            <span class="nav-title">发送方</span>
            <span class="nav-desc">签名与加密</span>
          </span>
        </router-link>
        <router-link to="/receiver" class="nav-item" active-class="nav-item--active">
          <span class="nav-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5"/><path d="M12 5l-7 7 7 7"/></svg>
          </span>
          <span class="nav-label">
            <span class="nav-title">接收方</span>
            <span class="nav-desc">解密与验证</span>
          </span>
        </router-link>
        <router-link to="/invoices" class="nav-item" active-class="nav-item--active">
          <span class="nav-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M9 3v18M3 9h6M3 15h6"/></svg>
          </span>
          <span class="nav-label">
            <span class="nav-title">发票管理</span>
            <span class="nav-desc">数据增删改查</span>
          </span>
        </router-link>
        <router-link to="/blockchain" class="nav-item" active-class="nav-item--active">
          <span class="nav-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M8 12h8M12 8v8"/></svg>
          </span>
          <span class="nav-label">
            <span class="nav-title">区块链</span>
            <span class="nav-desc">链上数据管理</span>
          </span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="status-dot"></div>
        <span>系统运行中</span>
      </div>
    </aside>

    <main class="main-area">
      <header class="topbar">
        <div class="topbar-breadcrumb">
          <span class="breadcrumb-current">{{ pageTitle }}</span>
        </div>
        <div class="topbar-actions">
          <span class="time-display">{{ currentTime }}</span>
        </div>
      </header>
      <div class="page-content">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const pageTitle = computed(() => route.meta?.title || '电子发票验签模拟系统')

const currentTime = ref('')
let timer = null
onMounted(() => {
  const update = () => {
    const now = new Date()
    currentTime.value = now.toLocaleString('zh-CN', { hour12: false })
  }
  update()
  timer = setInterval(update, 1000)
})
onUnmounted(() => clearInterval(timer))
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }

:root {
  --primary: #4F6EF7;
  --primary-dark: #3B54D4;
  --primary-light: #EEF1FE;
  --success: #22C55E;
  --warning: #F59E0B;
  --danger: #EF4444;
  --bg: #F0F2F8;
  --surface: #FFFFFF;
  --text: #1E293B;
  --text-secondary: #64748B;
  --border: #E2E8F0;
  --shadow-sm: 0 1px 2px rgba(0,0,0,.05);
  --shadow: 0 4px 24px rgba(0,0,0,.06);
  --shadow-lg: 0 12px 48px rgba(0,0,0,.08);
  --radius: 12px;
  --radius-sm: 8px;
  --transition: 0.2s cubic-bezier(.4,0,.2,1);
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background: var(--bg);
  color: var(--text);
  -webkit-font-smoothing: antialiased;
}

.app-shell { display: flex; min-height: 100vh; }

.sidebar {
  width: 240px;
  background: linear-gradient(180deg, #1E293B 0%, #0F172A 100%);
  display: flex; flex-direction: column;
  flex-shrink: 0;
  position: fixed; top: 0; left: 0; bottom: 0; z-index: 100;
}

.logo-area {
  padding: 24px 20px;
  display: flex; align-items: center; gap: 12px;
  border-bottom: 1px solid rgba(255,255,255,.08);
}
.logo-icon {
  width: 40px; height: 40px;
  background: linear-gradient(135deg, #4F6EF7, #7C5CFC);
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; flex-shrink: 0;
}
.logo-icon svg { width: 22px; height: 22px; }
.logo-text { display: flex; flex-direction: column; }
.logo-title { color: #fff; font-size: 14px; font-weight: 600; line-height: 1.3; }
.logo-sub { color: rgba(255,255,255,.4); font-size: 10px; letter-spacing: 0.5px; }

.nav-menu { flex: 1; padding: 12px 12px; display: flex; flex-direction: column; gap: 2px; }
.nav-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 14px; border-radius: 8px;
  text-decoration: none; color: rgba(255,255,255,.55);
  transition: all var(--transition); cursor: pointer;
}
.nav-item:hover { background: rgba(255,255,255,.06); color: rgba(255,255,255,.85); }
.nav-item--active {
  background: rgba(79,110,247,.2); color: #fff;
}
.nav-icon { width: 36px; height: 36px; border-radius: 8px; display: flex; align-items: center; justify-content: center; background: rgba(255,255,255,.06); flex-shrink: 0; }
.nav-icon svg { width: 18px; height: 18px; }
.nav-item--active .nav-icon { background: rgba(79,110,247,.3); }
.nav-label { display: flex; flex-direction: column; }
.nav-title { font-size: 13px; font-weight: 600; line-height: 1.3; }
.nav-desc { font-size: 10px; opacity: .5; }

.sidebar-footer {
  padding: 16px 20px; border-top: 1px solid rgba(255,255,255,.08);
  display: flex; align-items: center; gap: 8px;
  color: rgba(255,255,255,.35); font-size: 12px;
}
.status-dot {
  width: 7px; height: 7px; border-radius: 50%;
  background: #22C55E; box-shadow: 0 0 6px rgba(34,197,94,.5);
  animation: pulse 2s infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: .4; }
}

.main-area { flex: 1; margin-left: 240px; display: flex; flex-direction: column; min-height: 100vh; }
.topbar {
  height: 56px; background: var(--surface);
  border-bottom: 1px solid var(--border);
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 28px; position: sticky; top: 0; z-index: 50;
}
.breadcrumb-current { font-size: 15px; font-weight: 600; color: var(--text); }
.time-display { font-size: 12px; color: var(--text-secondary); font-family: monospace; }

.page-content { padding: 24px 28px; flex: 1; }

.page-fade-enter-active, .page-fade-leave-active { transition: opacity .2s ease, transform .2s ease; }
.page-fade-enter-from { opacity: 0; transform: translateY(8px); }
.page-fade-leave-to { opacity: 0; transform: translateY(-8px); }
</style>
