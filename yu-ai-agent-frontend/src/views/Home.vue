<template>
  <main class="home">
    <header class="topbar"><strong>启程 · 新生办事助手</strong><nav><span>演示版｜请以学校官方通知为准</span><router-link to="/admin">资料管理</router-link></nav></header>
    <section class="hero">
      <div class="eyebrow">CAMPUS STARTER</div>
      <h1>入学事项，<br><em>一件件办清楚。</em></h1>
      <p>从通知中寻找答案，核对来源，再整理属于你的办理清单。当前使用演示资料，不代表任何学校的规定。</p>
      <div class="actions"><router-link class="primary" to="/assistant">查询入学事项 →</router-link><router-link to="/checklist">生成办理清单</router-link><a href="#how">了解使用方式</a></div>
    </section>
    <section id="how" class="features">
      <article><span>01</span><h2>提出问题</h2><p>例如“住宿生报到要准备什么？”</p></article>
      <article><span>02</span><h2>核对依据</h2><p>查看回答引用的通知与对应段落。</p></article>
      <article><span>03</span><h2>整理清单</h2><p>结合校区和身份生成待办事项。</p></article>
    </section>
  </main>
</template>

<style scoped>
.home{min-height:100vh;background:#f4f1e8;color:#1c352f;font-family:system-ui,"Microsoft YaHei",sans-serif}.topbar{height:72px;padding:0 max(5vw,24px);display:flex;align-items:center;justify-content:space-between;border-bottom:1px solid #d9dfd1}.topbar strong{font-size:19px}.topbar nav{display:flex;gap:20px;align-items:center;font-size:13px}.topbar span{color:#687b70}.topbar nav a{color:#245c47;font-weight:700}.hero{max-width:1100px;margin:auto;padding:110px 24px 95px}.eyebrow{font-size:12px;letter-spacing:.3em;color:#568372;font-weight:700}.hero h1{font-size:clamp(42px,7vw,88px);line-height:1.15;letter-spacing:-.045em;margin:24px 0}.hero em{font-style:normal;color:#588b72}.hero p{font-size:18px;line-height:1.9;max-width:650px;color:#5d7065}.actions{display:flex;align-items:center;gap:30px;margin-top:38px;flex-wrap:wrap}.actions a{color:#2c5b48;text-decoration:none;font-weight:700}.actions .primary{background:#245c47;color:white;padding:16px 23px;border-radius:10px}.features{max-width:1100px;margin:auto;padding:0 24px 100px;display:grid;grid-template-columns:repeat(3,1fr);gap:16px}.features article{background:white;border:1px solid #e2e7dd;padding:30px;border-radius:16px}.features span{color:#76a488;font-weight:700}.features h2{font-size:22px;margin:28px 0 8px}.features p{color:#687b70;line-height:1.6}@media(max-width:650px){.topbar span{display:none}.hero{padding:75px 24px}.features{grid-template-columns:1fr}}
</style>
