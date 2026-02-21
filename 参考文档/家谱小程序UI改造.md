# 家谱小程序 UI 风格改造 & 家谱树展示逻辑改造 

## 一、总体背景与改造目标

我有一个微信小程序家谱应用，需要对其 **UI 风格** 和 **家谱树展示逻辑** 进行全面改造，以匹配提供的新设计稿风格。

**改造范围说明（重要）：**
- ✅ 必须改：全局 UI 视觉风格、家谱树展示组件及其交互逻辑
- ⛔ 不得改：数据库结构、用户鉴权流程、云函数、页面路由配置、其余业务逻辑
- ❓ 若改动超出上述范围，必须先向我确认，不得擅自修改

---

## 二、全局 UI 风格规范

### 2.1 Design Token（CSS 变量 / WXSS 变量）

在小程序的 `app.wxss` 中统一定义以下全局颜色变量（若小程序不支持原生变量，则用 Less/Sass 变量替代，并在每处引用时保持一致）：

```
--theme-bg:      #F2ECE4   /* 页面底色，温暖米黄 */
--theme-card:    #FBF9F6   /* 卡片/组件背景，浅暖白 */
--theme-text:    #3E2A23   /* 主文字色，深棕 */
--theme-primary: #8E292C   /* 品牌色，传统深红 */
--theme-border:  #D4C9BD   /* 边框色，暖灰 */
```

### 2.2 字体

全局使用衬线体字族，按优先级降级：

```
font-family: 'Noto Serif SC', 'Songti SC', 'SimSun', STSong, serif;
```

所有文字颜色默认使用 `--theme-text`（#3E2A23）。

### 2.3 页面底色

所有页面的 `page` / `body` 背景色设置为 `--theme-bg`（#F2ECE4）。

### 2.4 卡片组件规范

每个信息卡片（列表项、详情卡片等）统一遵循：
- 背景色：`--theme-card`（#FBF9F6）
- 边框：`1px solid --theme-border`，圆角 `6~8px`
- 阴影：`0 1px 4px rgba(0,0,0,0.06)`
- 内边距：`16px`

### 2.5 按钮规范

**主操作按钮（实心）：**
- 背景：`--theme-primary`（#8E292C）
- 文字：白色，`font-weight: bold`，`letter-spacing: 0.1em`
- 边框：`1px solid #722023`
- 点击态：`opacity: 0.9`

**次级操作按钮（描边）：**
- 背景：`--theme-card`
- 边框：`1px solid --theme-primary`
- 文字：`--theme-primary`
- 点击态：背景变为 `--theme-bg`

**危险/警示按钮：**
- 背景：`#F9EBEA`
- 边框：`1px solid #E6B0AA`
- 文字：`--theme-primary`

### 2.6 Section 标题样式

每个区块标题统一使用以下样式：左侧竖线 + 文字，示例结构：

```html
<view class="section-title">
  <view class="section-line"></view>
  <text>家族世系图</text>
</view>
```

```css
.section-title {
  display: flex;
  align-items: center;
  font-size: 14px;
  font-weight: bold;
  color: #6D4C41;
  margin-bottom: 12px;
}
.section-line {
  width: 4px;
  height: 14px;
  background-color: #8E292C;
  margin-right: 8px;
  border-radius: 2px;
}
```

### 2.7 提示/信息栏样式

黄色提示区（编修提示、注意事项等）：
- 背景：`#FFF8E1`
- 边框：`1px solid #FFE082`
- 主文字：`#8D6E63`
- 强调文字：`#F57F17`

红色标签（如"待核准"）：
- 背景：`#FDF2F1`
- 边框：`1px solid #E6B0AA`
- 文字：`--theme-primary`

### 2.8 全局头部（Header）

- 背景：`--theme-card`
- 下边框：`1px solid --theme-border`
- 标题：居中，`font-size: 17px`，`font-weight: bold`，`letter-spacing: 0.2em`，颜色 `--theme-text`
- 左侧返回按钮：文字"‹ 返回"，颜色 `--theme-text`，隐藏时不占位
- 顶部安全区：`padding-top: env(safe-area-inset-top)`（或小程序等价写法）

### 2.9 底部导航栏

- 背景：`--theme-card`
- 上边框：`1px solid --theme-border`
- 每个 tab：icon（emoji）+ 文字标签，文字 `10px`，`letter-spacing: 0.15em`
- 选中 tab 颜色：`--theme-primary`
- 未选中 tab 颜色：`#8D6E63`
- 消息角标：`--theme-primary` 背景，白色文字，圆形，绝对定位于 icon 右上角

---

## 三、家谱树展示逻辑改造

这是本次改造的核心部分，需要完整重写家谱树展示组件。

### 3.1 整体布局

家谱树展示区域：
- 外层容器：卡片样式（参考 2.4 卡片规范）
- 内层支持 **横向滚动**（`overflow-x: auto` / 小程序 `scroll-view` 横向模式）
- 树节点区域设置 `min-width: max-content`，防止折行

### 3.2 树形结构渲染逻辑

树结构采用 **纵向、父子缩进** 的传统家谱格式（而非横向展开的组织架构图）。渲染规则如下：

**父子连线规则：**
```
每个子节点容器（.tree-children）：
  - 左侧 padding-left: 28px
  - 左侧 margin-left: 12px
  - 左边框：1px solid --theme-border（形成纵向族谱连线）

每个节点项（.tree-node-item）：
  - margin-top: 16px
  - 伪元素 ::before 形成横向连线：
      position: absolute
      top: 22px（对齐节点卡片中心）
      left: -28px
      width: 28px
      border-top: 1px solid --theme-border
```

**最后一个子节点处理：**
最后一个 `.tree-node-item` 的子 `.tree-children::before`（纵向线）应隐藏，避免连线悬空。

### 3.3 节点卡片样式

每个成员节点由两部分组成：**折叠按钮** + **成员信息卡片**。

**成员信息卡片：**
```
- 背景：--theme-card
- 边框：1px solid --theme-border
- 圆角：4px
- 内边距：10px 12px
- 阴影：0 1px 3px rgba(0,0,0,0.08)
- 点击缩放反馈：scale(0.98)
```

卡片内容布局（水平）：
1. **性别标识块**（左）：
   - 尺寸：28×28px，圆角 2px
   - 男性：背景 `#EAF2FF`，文字 `#1565C0`，边框 `#1565C0`，显示"男"
   - 女性：背景 `#FDF2F1`，文字 `#8E292C`，边框 `#8E292C`，显示"女"
   - 字号：12px，font-weight: bold

2. **文字信息区**（右）：
   - 姓名：`15px`，`font-weight: bold`，`letter-spacing: 0.15em`，颜色 `--theme-text`
   - 世代标签：内联块，`10px`，颜色 `--theme-primary`，背景 `#FDF2F1`，边框 `1px solid #E6B0AA`，圆角 2px，内边距 `1px 4px`，显示"第 N 世"

### 3.4 折叠/展开按钮

仅当节点存在子节点时渲染此按钮：

```
- 位置：绝对定位，left: -10px（相对节点卡片父容器），垂直居中
- 尺寸：20×20px，圆角 2px
- 背景：--theme-card
- 边框：1px solid --theme-border
- 文字："-"（展开状态）/ "+"（折叠状态），颜色 --theme-primary
- 阴影：0 1px 3px rgba(0,0,0,0.1)
- z-index 高于卡片，防止被遮挡
```

**交互逻辑：**
- 点击按钮时，切换对应子节点容器（`children-{nodeId}`）的显示/隐藏
- 同步切换按钮文字 `"-"` / `"+"`
- 事件必须阻止冒泡（`stopPropagation`），避免误触打开成员操作菜单

### 3.5 成员操作菜单（底部抽屉）

点击成员卡片时，从底部弹出操作抽屉：

**抽屉样式：**
- 背景：`--theme-bg`
- 顶部圆角：16px
- 上方拖拽条：`48×4px`，颜色 `--theme-border`，居中，圆角
- 出现/消失动画：`translateY(100%)` → `translateY(0)`，时长 `300ms`，缓动 `cubic-bezier(0.4,0,0.2,1)`

**抽屉头部：**
- 成员姓名：`20px`，`font-weight: bold`，`letter-spacing: 0.15em`，颜色 `--theme-text`，居中
- 副文字：`"第 N 世成员"`，`12px`，颜色 `#8D6E63`，居中

**操作按钮列表（竖向排列，间距 12px）：**
1. 录入子嗣（下一世）→ 次级按钮样式，右侧图标 "↳"
2. 追溯先祖（上一世）→ 次级按钮样式，右侧图标 "↰"
3. 修正小传 → 警示按钮样式（#F5EBE9 背景），右侧图标 "✎"

所有按钮：`width: 100%`，`padding: 14px 24px`，`border-radius: 4px`，文字 `font-weight: bold`，`letter-spacing: 0.15em`

**遮罩层：**
- 点击遮罩层关闭抽屉
- 遮罩：`rgba(0,0,0,0.6)` + `backdrop-filter: blur(2px)`
- 渐显动画：`opacity 0~1`，时长 `300ms`

### 3.6 表单弹窗（居中模态框）

点击抽屉内的操作项后，关闭抽屉、打开居中模态表单：

**弹窗样式：**
- 外层遮罩（复用全局遮罩）
- 弹窗卡片：`max-width: 360px`，`border-radius: 8px`，背景 `--theme-card`，边框 `--theme-border`，阴影

**弹窗头部：**
- 背景：`--theme-bg`，下边框 `--theme-border`
- 标题：`font-size: 17px`，`font-weight: bold`，`letter-spacing: 0.15em`
- 右侧关闭按钮："×"，`24px`，颜色 `#8D6E63`

**表单内容区：**
- 标签：`14px`，`font-weight: bold`，颜色 `--theme-text`，`margin-bottom: 4px`
- 输入框：`width: 100%`，`border: 1px solid --theme-border`，`border-radius: 4px`，`padding: 10px 12px`，背景 `--theme-bg`，`focus` 时边框色变 `--theme-primary`
- 单选框：`accent-color: --theme-primary`

**表单底部按钮区（水平排列）：**
- 取消按钮："作罢"，次级样式
- 确认按钮："落笔确认"，主按钮样式

---

## 四、其他页面改造要求

### 4.1 首页（家谱列表页）

**顶部快捷操作区（2列 grid）：**
- 左按钮"敬修家谱"：主按钮样式，icon `⛩️`
- 右按钮"寻根加入"：描边按钮样式，icon `📜`
- 两列间距 `16px`，按钮高度约 `80px`，竖向居中排列 icon + 文字

**家谱列表项：**
- 卡片样式（参考 2.4）
- 左侧：`48×48px` 方形色块，圆角 4px，显示谱名首字，背景 `--theme-bg`，边框 `--theme-border`，文字 `--theme-primary`，`font-size: 20px`
- 右侧：谱名（`16px`，`font-weight: bold`，`letter-spacing: 0.15em`）+ 副文字（`已录 N 人 · 由我修撰`，`12px`，颜色 `#8D6E63`）
- 右箭头：`"›"`，颜色 `--theme-border`

### 4.2 家谱详情页

**邀请码信息区（卡片）：**
- 副标题："家族邀请码"，`12px`，颜色 `#8D6E63`
- 邀请码：`font-family: monospace`，`20px`，`font-weight: bold`，`letter-spacing: 0.3em`，颜色 `--theme-primary`
- "复制分享"按钮：警示按钮样式（稍小，`12px`）

### 4.3 审批页（奏报核准）

**提示栏：** 使用 2.7 黄色提示区样式

**审批卡片（每条申请）：**
- 顶部行：左侧"待核准"红色标签 + 家谱名；右侧申请人名
- 内容区：浅色背景区块，描述申请内容，被修改字段用 `--theme-primary` 颜色 + 下虚线强调
- 底部按钮：左"驳回"（次级按钮）+ 右"准奏入谱"（主按钮）

---

## 五、滚动条样式（WebView 侧）

若存在 WebView 渲染的 scroll 区域，统一设置细线滚动条：
```css
::-webkit-scrollbar { width: 4px; height: 4px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: #D4C9BD; border-radius: 4px; }
```

---

## 六、改造注意事项与禁止事项

1. **不得修改任何数据模型**（member 结构、genealogy 结构、request 结构等），字段增减须先询问我。
2. **不得修改路由跳转逻辑**，页面路径、tabBar 配置保持不变。
3. **不得修改云函数**及任何后端接口调用代码。
4. **不得修改审批流程的业务逻辑**（approve / reject 的数据操作）。
5. 若现有代码中有部分逻辑与本次树展示重构存在耦合（如父子关系计算、世代推算函数），**不得修改这些函数的逻辑**，仅可修改其调用结果的展示方式。
6. 所有新增 class 命名须加 `jpu-`（家谱UI）前缀，避免与现有 class 冲突。
7. 改动后需对以下场景进行自测验证，并告知我测试结果：
   - 家谱树正常渲染（多层级）
   - 折叠/展开按钮正常工作
   - 成员操作抽屉正常弹出/关闭
   - 表单提交后树正常刷新

---

## 七、交付要求

- 以文件为单位列出所有改动文件及改动摘要
- 新增的样式集中写入对应页面的 `.wxss` 文件，不要内联 style
- 若使用了新的第三方组件库，须提前告知我
- 改造完成后保留原始代码注释，新增代码注释用中文书写
