<template>
  <main class="admin">
    <header><router-link to="/">← 返回首页</router-link><strong>资料管理</strong></header>
    <div class="layout">
      <section class="panel">
        <h1>上传资料</h1>
        <p>支持 UTF-8 编码的 Markdown、TXT 和可提取文字的 PDF。这里上传的资料会替换内置示例，停用后不参与查询。</p>
        <form @submit.prevent="upload">
          <label>管理员令牌<input v-model="token" type="password" autocomplete="off" required /></label>
          <label>资料标题<input v-model="form.title" required /></label>
          <label>原文链接<input v-model="form.sourceUrl" type="url" placeholder="https://..." /></label>
          <div class="row"><label>学校<input v-model="form.school" required /></label><label>校区<input v-model="form.campus" required /></label></div>
          <label>适用人群<input v-model="form.audience" required /></label>
          <div class="row"><label>发布日期<input v-model="form.publishedAt" type="date" /></label><label>有效期至<input v-model="form.validUntil" type="date" /></label></div>
          <label>检索关键词（逗号分隔）<input v-model="form.keywords" placeholder="报到,材料,住宿" required /></label>
          <label>资料文件<input type="file" accept=".md,.txt,.pdf" @change="chooseFile" required /></label>
          <button :disabled="busy">上传并启用</button>
        </form>
        <p v-if="message" :class="error ? 'error' : 'success'">{{ message }}</p>
      </section>
      <section class="panel">
        <div class="list-header"><div><h2>已上传资料</h2><p>输入令牌后读取列表。</p></div><button class="secondary" @click="loadDocuments" :disabled="busy || !token">刷新</button></div>
        <p v-if="!documents.length" class="empty">暂无资料，或尚未加载。</p>
        <article v-for="document in documents" :key="document.id" class="document">
          <div><strong>{{ document.title }} · v{{ document.version }}</strong><span :class="document.active ? 'active' : 'inactive'">{{ document.active ? '已启用' : '已停用' }}</span></div>
          <p>{{ document.school }} · {{ document.campus }} · {{ document.audience }}</p>
          <p>关键词：{{ document.keywords.join('、') }}<br>有效期：{{ document.validUntil || '未设置' }}</p>
          <button v-if="document.active" class="secondary" @click="deactivate(document.id)" :disabled="busy">停用</button>
        </article>
      </section>
    </div>
  </main>
</template>

<script setup>
import { ref, reactive } from 'vue'

const token = ref('')
const file = ref(null)
const documents = ref([])
const busy = ref(false)
const message = ref('')
const error = ref(false)
const form = reactive({ title: '', sourceUrl: '', school: '演示学校', campus: '全部校区', audience: '全体新生', publishedAt: '', validUntil: '', keywords: '' })
function chooseFile(event) { file.value = event.target.files?.[0] || null }
function notify(text, isError = false) { message.value = text; error.value = isError }
async function request(url, options = {}) {
  const response = await fetch(url, { ...options, headers: { 'X-Admin-Token': token.value, ...options.headers } })
  if (!response.ok) throw new Error(`请求失败（${response.status}）`)
  return response.status === 204 ? null : response.json()
}
async function loadDocuments() {
  busy.value = true
  try { documents.value = await request('/api/campus/documents'); notify('资料列表已更新') }
  catch (e) { notify(e.message, true) }
  finally { busy.value = false }
}
async function upload() {
  if (!file.value) return notify('请选择资料文件', true)
  busy.value = true
  try {
    const data = new FormData()
    for (const [key, value] of Object.entries(form)) if (value) data.append(key, value)
    data.append('file', file.value)
    await request('/api/campus/documents', { method: 'POST', body: data })
    notify('上传成功，资料已参与查询')
    await loadDocuments()
  } catch (e) { notify(e.message, true) }
  finally { busy.value = false }
}
async function deactivate(id) {
  busy.value = true
  try { await request(`/api/campus/documents/${id}/deactivate`, { method: 'POST' }); notify('资料已停用'); await loadDocuments() }
  catch (e) { notify(e.message, true) }
  finally { busy.value = false }
}
</script>

<style scoped>
.admin{min-height:100vh;background:#f4f1e8;color:#1c352f;font-family:system-ui,"Microsoft YaHei",sans-serif}header{height:68px;padding:0 5vw;display:flex;align-items:center;gap:24px;border-bottom:1px solid #d9dfd1}a{color:#486b58;text-decoration:none}.layout{max-width:1200px;margin:35px auto;padding:0 20px;display:grid;grid-template-columns:1fr 1fr;gap:22px}.panel{background:white;border:1px solid #e0e7dc;border-radius:15px;padding:28px}.panel h1,.panel h2{font-size:26px;margin:0 0 12px}.panel p{color:#627668;line-height:1.7}form{display:grid;gap:14px;margin-top:24px}label{display:grid;gap:6px;font-size:13px;font-weight:700}input{width:100%;border:1px solid #d5dfd3;border-radius:8px;padding:11px;font:inherit}.row{display:grid;grid-template-columns:1fr 1fr;gap:12px}button{border:0;background:#245c47;color:white;padding:12px 18px;border-radius:8px;cursor:pointer;font-weight:700}.secondary{border:1px solid #c9dacb;background:white;color:#245c47}.list-header{display:flex;justify-content:space-between;align-items:start}.document{border-top:1px solid #e6ece2;padding:20px 0}.document>div{display:flex;justify-content:space-between;align-items:center}.document p{font-size:13px}.active,.inactive{font-size:12px;padding:5px 9px;border-radius:20px}.active{background:#e5f3e6;color:#316448}.inactive{background:#eee;color:#777}.empty{padding:40px 0;text-align:center}.error{color:#b34a3d!important}.success{color:#2f7953!important}@media(max-width:800px){.layout{grid-template-columns:1fr}.row{grid-template-columns:1fr}}
</style>
