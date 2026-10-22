<template>
  <main class="page">
    <header><router-link to="/">← 返回首页</router-link><strong>入学事项查询</strong><span>演示资料</span></header>
    <section class="shell">
      <aside><div class="eyebrow">使用提示</div><h1>从一个问题开始</h1><p>请先询问报到、住宿或校园卡等事项。正式学校资料接入前，回答仅供功能演示。</p><label>校区<input v-model="campus" placeholder="可选，如：东校区"></label><label>学生类型<input v-model="audience" placeholder="可选，如：本科新生"></label><div class="suggestions"><button v-for="q in suggestions" :key="q" @click="ask(q)">{{ q }} ↗</button></div></aside>
      <section class="chat"><div class="messages"><div v-for="(item,index) in messages" :key="index" :class="['bubble',item.role]"><small>{{item.role==='user'?'我':'启程助手'}}</small><p>{{item.text}}</p><div v-if="item.citations?.length" class="citations"><strong>资料来源</strong><div v-for="(citation,i) in item.citations" :key="i">[{{i+1}}] {{citation.title}} · {{citation.section}}<a v-if="citation.sourceUrl" :href="citation.sourceUrl" target="_blank" rel="noopener">查看原文 ↗</a></div></div></div><div v-if="loading" class="loading">正在查询资料…</div></div><form @submit.prevent="ask(input)"><input v-model="input" :disabled="loading" placeholder="输入你的入学问题" aria-label="入学问题"/><button :disabled="loading || !input.trim()">发送</button></form></section>
    </section>
  </main>
</template>

<script setup>
import { ref } from 'vue'

const suggestions = ['报到时通常要核对哪些材料？', '住宿生应该关注什么？', '在哪里查看校园卡办理通知？']
const messages = ref([{ role: 'assistant', text: '你好！这里先使用演示资料。告诉我你想了解的入学事项，我会尽量给出可核对的来源。' }])
const input = ref('')
const campus = ref('')
const audience = ref('')
const loading = ref(false)

async function ask(question) {
  if (!question?.trim() || loading.value) return
  messages.value.push({ role: 'user', text: question.trim() })
  input.value = ''
  loading.value = true
  try {
    const params = new URLSearchParams({ q: question.trim(), campus: campus.value, audience: audience.value })
    const response = await fetch(`/api/campus/ask?${params}`)
    if (!response.ok) throw new Error(`服务返回 ${response.status}`)
    const result = await response.json()
    messages.value.push({ role: 'assistant', text: result.answer, citations: result.citations })
  } catch (error) {
    messages.value.push({ role: 'assistant', text: `暂时无法连接服务：${error.message}` })
  } finally { loading.value = false }
}
</script>

<style scoped>
.page{min-height:100vh;background:#f4f1e8;color:#1c352f;font-family:system-ui,"Microsoft YaHei",sans-serif}header{height:68px;padding:0 max(4vw,20px);display:flex;align-items:center;gap:24px;border-bottom:1px solid #d9dfd1}header a{color:#486b58;text-decoration:none}header strong{font-size:18px}header span{margin-left:auto;background:#e3eedf;color:#4b765d;padding:6px 10px;border-radius:20px;font-size:12px}.shell{max-width:1200px;margin:40px auto;padding:0 20px;display:grid;grid-template-columns:330px 1fr;gap:25px}aside{padding:30px}aside .eyebrow{color:#6b9b79;font-size:12px;letter-spacing:.2em}aside h1{font-size:34px;line-height:1.2}aside p{color:#667a6c;line-height:1.8}aside label{display:grid;gap:6px;margin-top:12px;font-size:13px;font-weight:700}aside input{width:100%;border:1px solid #dbe4d8;border-radius:8px;padding:10px;font:inherit}.suggestions{display:grid;gap:10px;margin-top:30px}.suggestions button{text-align:left;border:1px solid #d6e2d4;border-radius:9px;background:#fff;padding:15px;color:#315945;cursor:pointer}.chat{height:calc(100vh - 150px);min-height:560px;background:white;border:1px solid #e2e7dd;border-radius:16px;display:flex;flex-direction:column;overflow:hidden}.messages{flex:1;overflow:auto;padding:26px;display:flex;flex-direction:column;gap:16px}.bubble{max-width:78%;padding:14px 18px;border-radius:13px;background:#f1f5ee;line-height:1.7;white-space:pre-wrap}.bubble.user{align-self:flex-end;background:#245c47;color:white}.bubble small{font-weight:700;opacity:.7}.bubble p{margin:8px 0 0}.citations{border-top:1px solid #dce6d9;margin-top:12px;padding-top:10px;font-size:12px;color:#52725d}.citations div{margin-top:6px}.citations a{margin-left:8px;color:#245c47}.loading{color:#6b806e}form{border-top:1px solid #e4e9e1;padding:15px;display:flex;gap:10px}form input{flex:1;border:1px solid #dbe4d8;border-radius:8px;padding:13px;font-size:15px}form button{border:0;border-radius:8px;background:#245c47;color:white;padding:0 24px;cursor:pointer}button:disabled{opacity:.5;cursor:default}@media(max-width:800px){.shell{grid-template-columns:1fr;margin:10px auto}aside{padding:15px}.chat{height:65vh;min-height:480px}}
</style>
