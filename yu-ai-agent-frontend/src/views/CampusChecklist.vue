<template>
  <main class="page">
    <header><router-link to="/">← 返回首页</router-link><strong>办理清单</strong></header>
    <section class="content">
      <div class="intro"><span>PERSONAL CHECKLIST</span><h1>把入学事项<br>整理成一张清单。</h1><p>选择你的情况，系统只会从适用资料中提取明确标记的事项。没有依据的材料、地点和截止时间会显示待核实。</p></div>
      <div class="card"><form @submit.prevent="generate"><label>校区<input v-model="campus" placeholder="如：东校区" required></label><label>学生类型<input v-model="audience" placeholder="如：本科新生" required></label><label class="housing"><input v-model="housing" type="checkbox">我是住宿生</label><button :disabled="loading">生成清单</button></form>
        <div v-if="error" class="error">{{error}}</div>
        <div v-if="result" class="result"><p class="note">{{result.note}}</p><p v-if="!result.items.length">当前条件下没有找到明确的清单事项。</p><article v-for="(item,index) in result.items" :key="index"><div class="number">{{String(index+1).padStart(2,'0')}}</div><div><h2>{{item.title}}</h2><p>材料：{{item.materials || '待核实'}}　地点：{{item.location || '待核实'}}　截止：{{item.deadline || '待核实'}}</p><small>来源：{{item.citation.title}} · {{item.citation.section}} <a v-if="item.citation.sourceUrl" :href="item.citation.sourceUrl" target="_blank" rel="noopener">查看原文 ↗</a></small></div></article></div>
      </div>
    </section>
  </main>
</template>

<script setup>
import { ref } from 'vue'
const campus = ref('全部校区')
const audience = ref('全体新生')
const housing = ref(false)
const loading = ref(false)
const error = ref('')
const result = ref(null)
async function generate() {
  loading.value = true
  error.value = ''
  try {
    const params = new URLSearchParams({ campus: campus.value, audience: audience.value, housing: String(housing.value) })
    const response = await fetch(`/api/campus/checklist?${params}`)
    if (!response.ok) throw new Error(`服务返回 ${response.status}`)
    result.value = await response.json()
  } catch (e) { error.value = e.message }
  finally { loading.value = false }
}
</script>

<style scoped>
.page{min-height:100vh;background:#f4f1e8;color:#1c352f;font-family:system-ui,"Microsoft YaHei",sans-serif}header{height:68px;padding:0 5vw;display:flex;align-items:center;gap:24px;border-bottom:1px solid #d9dfd1}a{color:#245c47;text-decoration:none}.content{max-width:1150px;margin:70px auto;padding:0 20px;display:grid;grid-template-columns:1fr 1.2fr;gap:70px}.intro span{font-size:12px;letter-spacing:.25em;color:#679478;font-weight:700}.intro h1{font-size:clamp(34px,4vw,54px);line-height:1.25;margin:22px 0}.intro p{color:#63756a;line-height:1.9;max-width:430px}.card{background:white;border:1px solid #e0e7dc;border-radius:16px;padding:30px;min-height:420px}form{display:grid;gap:16px}label{display:grid;gap:6px;font-weight:700;font-size:13px}input:not([type=checkbox]){padding:12px;border:1px solid #d6e0d4;border-radius:8px;font:inherit}.housing{display:flex;align-items:center;gap:8px}.housing input{width:17px;height:17px}button{border:0;border-radius:8px;background:#245c47;color:white;padding:14px;cursor:pointer;font-weight:700}.result{margin-top:32px;border-top:1px solid #e3e9e0;padding-top:22px}.note{color:#6a786c;line-height:1.6}article{display:flex;gap:16px;border-bottom:1px solid #edf0ec;padding:20px 0}.number{color:#73a283;font-weight:800}article h2{font-size:17px;margin:0 0 8px}article p{font-size:13px;color:#596c60;line-height:1.7}article small{color:#668270}.error{color:#b34a3d;margin-top:15px}@media(max-width:800px){.content{grid-template-columns:1fr;gap:22px;margin:35px auto}}
</style>
