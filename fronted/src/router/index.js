import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/sender'
  },
  {
    path: '/sender',
    name: 'Sender',
    component: () => import('../views/SenderView.vue'),
    meta: { title: '发送方 - 签名加密' }
  },
  {
    path: '/receiver',
    name: 'Receiver',
    component: () => import('../views/ReceiverView.vue'),
    meta: { title: '接收方 - 解密验证' }
  },
  {
    path: '/invoices',
    name: 'Invoices',
    component: () => import('../views/InvoiceView.vue'),
    meta: { title: '发票管理' }
  },
  {
    path: '/blockchain',
    name: 'Blockchain',
    component: () => import('../views/BlockchainView.vue'),
    meta: { title: '区块链' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
