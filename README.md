# 启程 · 新生办事助手

基于 [yu-ai-agent](https://github.com/liyupi/yu-ai-agent) 二次开发的高校新生办事助手。当前版本使用**演示资料**，不代表任何真实学校的规定。

## 已实现

- Markdown、TXT、可提取文字的 PDF 资料上传；记录学校、校区、适用人群、发布日期、有效期及来源链接。
- 相同资料重复上传拦截；同名资料更新后自动停用旧版本。
- 按关键词检索资料段落，回答附来源与段落或 PDF 页码；无依据时拒答。
- 按校区、学生类型和住宿情况生成清单。清单事项从 Markdown 的复选框行提取，缺失字段显示“待核实”。
- 可选的 Ollama 向量检索与 DeepSeek 或 DashScope 回答生成。默认关闭，基础功能无需模型密钥。
- 管理员资料列表及停用功能。

## 本地运行

需要 Java 21+、Node.js。仓库内含 Maven Wrapper。

```powershell
$env:CAMPUS_ADMIN_TOKEN='请换成自己的本地令牌'
.\mvnw.cmd -DskipTests package
java -jar target\yu-ai-agent-0.0.1-SNAPSHOT.jar
```

另开终端：

```powershell
cd yu-ai-agent-frontend
npm ci
npm run dev
```

打开 http://localhost:3000/ 。后端接口位于 http://localhost:8123/api/ 。打开“资料管理”后输入本地令牌，即可上传 [演示 Markdown](sample-data/demo-campus.md)。

**演示资料与正式资料的切换：**没有启用的上传资料时，问答和清单使用内置演示数据。存在已启用的资料时，仅使用上传资料。停用后不再引用旧资料。

## 可选模型模式

- 本地向量检索：先启动 Ollama 并安装所需 embedding 模型，再设置 `CAMPUS_VECTOR_ENABLED=true` 和 `CAMPUS_EMBEDDING_MODEL`。默认模型为 `nomic-embed-text`，对当前中文演示资料的排序效果有限；上线前必须用目标学校资料评估并选择合适的中文 embedding 模型。相似度门槛可通过 `CAMPUS_VECTOR_MIN_SIMILARITY` 调整。
- DeepSeek 回答生成：将 [secrets.properties.example](secrets.properties.example) 复制为项目根目录的 `secrets.properties`，填入 `campus.deepseek.api-key` 后重启后端。默认使用 `deepseek-flash`；可用 `campus.deepseek.model` 调整。也可通过环境变量 `CAMPUS_AI_PROVIDER=deepseek`、`DEEPSEEK_API_KEY` 配置。程序会把用户问题和检索到的资料片段发送给 DeepSeek，上传真实资料前请先确认是否适合交给第三方模型处理。
- DashScope 回答生成：设置 `CAMPUS_AI_PROVIDER=dashscope` 与 `DASHSCOPE_API_KEY`。两种模型模式均先检索资料；无检索结果时不会调用模型。生成失败或引用编号无效时回退到资料摘录。[DeepSeek API 文档](https://api-docs.deepseek.com/api/create-chat-completion/)说明了当前接口格式。

不要将真实密钥写入仓库。资料上传接口要求请求头 `X-Admin-Token`，令牌来自 `CAMPUS_ADMIN_TOKEN`。

## 资料写法

在 Markdown 中用二级标题标记段落；用以下形式提供可生成清单的事项：

```md
## 报到事项
- [ ] 核对报到安排 | 材料：录取通知书 | 地点：以官方通知为准 | 截止：以官方通知为准
```

上传时填写检索关键词，用逗号分隔。正式资料应填写可核查的学校原文 URL。PDF 必须包含可提取的文字；扫描图片型 PDF 暂不支持 OCR。

## 验证

另见 [仿真测试计划](docs/simulation-plan.md) 与 [仿真测试结果](docs/simulation-results.md)。7 份可上传的虚构通知 PDF 位于 [output/pdf](output/pdf)。上传时按 [资料清单](sample-data/simulation-manifest.json) 填写校区、适用人群、有效期和关键词。

```powershell
.\mvnw.cmd -q '-Dtest=DemoKnowledgeServiceTest,ChecklistServiceTest,CampusAnswerServiceTest,DocumentTextExtractorTest,DeepSeekAnswerGeneratorTest' test
cd yu-ai-agent-frontend
npm run build
```

当前测试覆盖命中与拒答、校区/人群过滤、上传停用、版本更新、PDF 提取、清单条件、模型生成失败回退以及 DeepSeek 请求格式。正式学校资料接入后，需要再建立真实问题集，测量回答正确性、引用有效性和拒答效果。

## 下一阶段

当前资料目录持久化在 `tmp/campus-documents.json`，检索默认使用关键词。目标学校及官方资料确定后，需完成 PostgreSQL + pgvector 持久化、真实资料评估、引用链接校验与部署。原仓库的恋爱大师和通用 YuManus 代码保留供学习，但新应用入口只加载 `com.yupi.yuaiagent.campus`。
