import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

export default {
  // 发送方：签名加密
  signXml(xmlContent) {
    return api.post('/crypto/sign', { xmlContent })
  },
  // 接收方：验证签名
  verify(cipherText, digitalSignature) {
    return api.post('/crypto/verify', { cipherText, digitalSignature })
  },
  // 生成数字指纹
  generateFingerprint(content) {
    return api.post('/crypto/fingerprint', { content })
  },
  // 生成数字签名
  generateSignature(fingerprint) {
    return api.post('/crypto/signature', { fingerprint })
  },
  // 对称加密
  encrypt(plainText, digitalSignature) {
    return api.post('/crypto/encrypt', { plainText, digitalSignature })
  },
  // 对称解密
  decrypt(cipherText) {
    return api.post('/crypto/decrypt', { cipherText })
  },
  // 解析XML
  parseXml(xmlContent) {
    return api.post('/crypto/parse-xml', { xmlContent })
  },
  // 保存发票
  saveInvoice(data) {
    return api.post('/invoice/save', data)
  },
  // 查询发票列表
  listInvoices() {
    return api.get('/invoice/list')
  },
  // 删除发票
  deleteInvoice(id) {
    return api.delete(`/invoice/${id}`)
  },
  // 更新发票
  updateInvoice(id, data) {
    return api.put(`/invoice/${id}`, data)
  },
  // 上链
  addBlock(invoiceId, invoiceData) {
    return api.post('/blockchain/add', { invoiceId, invoiceData })
  },
  // 获取链
  getChain() {
    return api.get('/blockchain/chain')
  },
  // 验证链
  validateChain() {
    return api.get('/blockchain/validate')
  },
  // 区块详情
  getBlockDetail(id) {
    return api.get(`/blockchain/block/${id}`)
  },
  // OCR 发票识别
  ocrConvert(file) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/ocr/convert', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 60000
    })
  }
}
