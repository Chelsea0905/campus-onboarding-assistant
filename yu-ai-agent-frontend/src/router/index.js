import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', component: () => import('../views/Home.vue'), meta: { title: '启程 · 新生办事助手' } },
  { path: '/assistant', component: () => import('../views/CampusAssistant.vue'), meta: { title: '查询入学事项 · 启程' } },
  { path: '/checklist', component: () => import('../views/CampusChecklist.vue'), meta: { title: '办理清单 · 启程' } },
  { path: '/admin', component: () => import('../views/CampusAdmin.vue'), meta: { title: '资料管理 · 启程' } }
]

const router = createRouter({ history: createWebHistory(), routes })
router.beforeEach(to => { document.title = to.meta.title || '启程 · 新生办事助手' })
export default router
