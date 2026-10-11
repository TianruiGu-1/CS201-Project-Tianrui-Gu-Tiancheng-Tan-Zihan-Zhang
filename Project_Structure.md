# 项目结构与功能说明

> COMPSCI 201 Final Group Project — DKU Campus Navigator
> 本文说明每个文件负责什么、为什么这样分。用途有两个:自己读懂每个文件(说明书要求
> "must understand and be able to explain all submitted code"),以及写报告时的速查。

---

## 一、一句话说清这个程序

读出校园路网(150 个节点、207 条路),让用户点两个地点,用 **Dijkstra** 算出两者之间
"最省"的一条路,画在校园地图上。所谓"最省",不只是走得短,经过**门节点**还要额外付出
一段**等效距离**。

---

## 二、目录结构

```
Graphic_Cases/
│
├── DKUApp/                     ← 提交的就是这个文件夹(8 个文件)
│   ├── DKUApp.java             入口,只有这里有 main
│   ├── Model.java              后端:持有图 + 用户选了什么
│   ├── CampusGraph.java        后端:图本身(邻接表)+ 边权怎么定
│   ├── CampusData.java         后端:150 个点的数据表【生成的,别手改】
│   ├── Node.java               后端:一个点的数据结构
│   ├── Dijkstra.java           后端:最短路算法
│   ├── AppFrame.java           前端:窗口、右侧面板
│   ├── CanvasPanel.java        前端:画布(地图 / 路线 / 标记 / 标签)
│   └── campus-map.jpg          底图【生成的】
│
├── tools/                      ← 构建脚本,不进提交
│   ├── make_campus_data.py     从 JSON 生成 CampusData.java
│   └── make_campus_map.py      从 PDF 裁出 campus-map.jpg
│
├── campus-graph-20261011-0015.json   数据源(组员导出)
├── DKU-Campus-Map-2024-官方最新版.pdf  地图源
├── AI_Usage_Log.md             AI 使用记录
├── Project_Structure.md        本文件
└── Map_Data.md                 数据收集模板
```

**提交时压缩 `DKUApp/` 一个文件夹即可。** 助教解压后 `javac *.java` + `java DKUApp` 就能跑,
不依赖任何外部库、不依赖工作目录、不读取 `DKUApp/` 以外的任何文件。

---

## 三、分层

```
        用户点击
           │
           ▼
   ┌───────────────┐   读状态    ┌──────────────────┐
   │  前端 (VIEW)  │◄───────────│  后端 (MODEL)     │
   │  AppFrame     │            │  Model            │
   │  CanvasPanel  │───────────►│  └ CampusGraph    │
   └───────────────┘  调方法     │     └ CampusData  │
                               │        └ Node     │
                               │     └ Dijkstra    │
                               └──────────────────┘
```

铁律两条(代码注释里也写着):

1. **前端不算。** 前端只从 Model 读状态然后画,自己不推导任何东西。
2. **后端不碰 Swing。** `Model` / `CampusGraph` / `CampusData` / `Node` / `Dijkstra`
   这五个文件**一行 Swing 都没 import**,可以脱离界面单独测试。

---

## 四、逐个文件说明

### 1. `DKUApp.java` — 26 行 — 入口

整个项目**唯一有 `main` 的文件**。做两件事:建 Model,建窗口。

```java
SwingUtilities.invokeLater(() -> {
    Model model = new Model();
    new AppFrame(model).setVisible(true);
});
```

**为什么单独一个文件:** 说明书要求助教能一眼找到程序入口。`SwingUtilities.invokeLater`
是 Swing 的规矩 —— 界面必须在事件线程上创建。

---

### 2. `Node.java` — 56 行 — 一个点

图里的一个节点。五个字段:

| 字段 | 含义 |
|---|---|
| `name` | 名字(`"Library - East Entrance"` 或 `"节点 8"`) |
| `x`, `y` | 坐标,**原始地图单位** |
| `selectable` | 是不是**正式节点**(能点的、有名字的)。97 个路径点是 `false` |
| `gateCost` | **等效距离**。44 个门是 `100`,其余 106 个是 `0` |

关键方法 `distanceTo(other)` —— 两点直线距离。**所有路长都由它算出来,没有一个是手填的。**

**为什么单独一个文件:** 它是整个项目最基本的数据单位,值得自己一个文件、自己一段注释。

---

### 3. `CampusData.java` — 430 行 — 数据表【生成的文件】

150 个点、207 条路的完整清单,以及视口参数。长这样:

```java
public static final double VIEW_LEFT = 1889.01;
public static final double VIEW_TOP  = 3343.85;
public static final double VIEW_SCALE = 0.294265;
...
g.addNode(new Node("Residence Hall", 2977.36, 2435.38, true, 0));
g.addNode(new Node("节点 8",        2585.62, 2442.85, false, 0));
...
g.connect("Residence Hall", "节点 10");   // 只写两端,长度不填
```

**注意 `connect()` 只给了两端,没给长度** —— 长度由 `CampusGraph` 从坐标算。
这样图上的位置和算出来的距离**不可能互相矛盾**(手工填数字迟早会填错)。

**为什么是 .java 而不是读 JSON:** 说明书要求解压就能编译运行。读外部文件的话,
助教的目录结构一不一样就可能找不到文件。

**这个文件是 `tools/make_campus_data.py` 生成的,不要手改** —— 改了下次重新生成就被覆盖。

---

### 4. `CampusGraph.java` — 172 行 — 图本身

**邻接表**:`Map<String, Node> byName` + `Map<String, List<Road>> adjacency`。

主要方法:

| 方法 | 干什么 |
|---|---|
| `addNode(n)` | 加一个点 |
| `connect(a, b)` | 连一条路 |
| `roadsFrom(name)` | 从这个点出发的所有路(算法靠它遍历) |
| `routeLength(route)` | 一条路线的总代价 |
| `isConnected()` | 连通性检查(启动时跑,数据坏了会报警) |
| `getRoadSegments()` | 每条路一次,给前端画线用 |

**这个文件最重要的一段是 `connect()` —— 门费在这里进边权:**

```java
public void connect(String a, String b) {
    Node from = require(a);
    Node to = require(b);
    double gap = from.distanceTo(to);
    adjacency.get(a).add(new Road(to, gap + to.getGateCost()));   // 走进 b,付 b 的门费
    adjacency.get(b).add(new Road(from, gap + from.getGateCost())); // 走进 a,付 a 的门费
}
```

**为什么两个方向分开加:** 门费只收**到达端**。从 A 走进门 G 要付 100,
从 G 走出来不付。所以同一个 `Road` 的两个方向权重通常不一样 —— **图是有向的,
尽管校园本身是无向的**。

**`routeLength()` 里有一次扣减:**

```java
return total - route.get(route.size() - 1).getGateCost();   // 终点免收
```

因为边权把"走进终点"的那次门费也算进去了,但路线是**结束**在终点、不是**穿过**它,
所以最后一次要退掉。起点天然不用退 —— 没有任何一条边是"走进起点"的。

---

### 5. `Dijkstra.java` — 94 行 — 最短路算法

**这个文件不知道门的存在。** 门费在建图时就已经进了边权,所以这里就是一个
**教科书版的 Dijkstra**,取权重那行写的是 `road.weight` 而不是什么距离。

```
反复做:
  1. 在所有还没"定下来"的点里,挑当前已知距离最小的那个 → current
  2. 如果 current 就是终点,结束
  3. 把 current 标记为"已定"
  4. 松弛:对 current 的每条出边 u→v,若 d(current)+w < d(v),则更新 d(v) 和来源
```

**为什么这样是对的:** 每次挑的是**全局最小**。一旦某个点被挑中,任何绕路都不可能更短,
因为路权永远非负(只加不减)。所以它被挑中的那一刻,它的距离就已经是最终答案。

**为什么不用优先队列:** 只有 150 个点,线性扫描找最小值比维护堆更简单也更快。
上千个点时才值得换。

**`rebuild()`** 是私有的辅助方法:终点记着"我是从哪儿来的",一路往回走就还原出整条路线。

---

### 6. `Model.java` — 147 行 — 状态中枢

**前端唯一的对话对象。** 持有 graph、起点、终点、算出来的路线。

| 方法 | 干什么 |
|---|---|
| `clickAt(x, y)` | 处理一次点击:找最近的可点节点,然后推移选择状态 |
| `getRoute()` | 当前路线(含路径点) |
| `getPublicStops()` | **只留正式节点**,给右侧文字用 |
| `getRouteLength()` | 总代价(含门费、已扣终点) |
| `isShowingWaypoints()` / `setShowingWaypoints()` | 路径点开关 |
| `reset()` | 清空 |

**选择状态机**(点一下推进一步):

```
什么都没选  ──点──►  [起点]  ──点──►  [起点, 终点]  ──点──►  [新起点]
```

**`nearestSelectable()` 只在前 53 个正式节点里找。** 97 个路径点在地图上不标名字,
如果允许点到它们,用户会觉得程序在乱选。

**为什么一个显示开关放在 Model 里:** 因为规则是"画面上出现的东西都从 Model 读"。
这样前端不持有任何自己的状态,`refresh()` 一处就能保证窗口是诚实的。

---

### 7. `CanvasPanel.java` — 248 行 — 画布

**整个项目唯一做坐标转换的地方。**

数据用的是地图自己的坐标系:原点在**左下角**、y **向上**。
Swing 是原点在**左上角**、y **向下**。两者相反。

```java
static int sx(double x) { return (int) Math.round((x - VIEW_LEFT) * VIEW_SCALE); }
static int sy(double y) { return (int) Math.round((VIEW_TOP  - y) * VIEW_SCALE); }
```

点击时反着算回去(`mapX` / `mapY`),再交给 Model。**Model 从头到尾没见过屏幕像素。**

绘制顺序(后画的盖住先画的):

```
drawMap        底图 + 一层白色蒙版(把地图压暗,让红线跳出来)
drawEdges      我们真正在搜索的路网(淡线)
drawRoute      当前路线(红线,4px 圆头)
drawNodes      路径点(开关打开时)/ 53 个正式节点 / 标签
```

**标签只给 53 个正式节点画**,并且带一圈白色描边 —— 地图深色区域上文字才看得清。

---

### 8. `AppFrame.java` — 143 行 — 窗口

左边是画布,右边 220px 是控制面板。面板上是四个**会变**的 `JLabel`(From / To /
Distance / Route),上面两个标题(`Campus Navigator`、`Route`)和下面的复选框、Reset 按钮。

**`refresh()` 是全项目最重要的一处约定:**

```java
private void refresh() {
    canvas.repaint();          // ← 这一行
    fromLabel.setText(...);
    ...
}
```

所有改动了 Model 的操作(点画布、勾复选框、按 Reset)**都必须经过 `refresh()`**。
因为这个程序是事件驱动的:算出了路线,还得有人告诉画布"该重画了"。

> 项目早期踩过一个坑就是漏了这行 —— 数据全对、右侧文字全对,地图上就是没有红线。
> 详见 `AI_Usage_Log.md` 的 Bug 3。

**路线文字用的是 `getPublicStops()` 不是 `getRoute()`** —— 一条 10 个节点的路线上
可能只有 2 个正式节点,其余 8 个是拐弯的地方,列出来只会把有用的名字淹没。

---

### 9. `campus-map.jpg` — 底图【生成的文件】

从官方 PDF 里那张 8305×4658 的原图裁出来的,存成画布的两倍分辨率(2482×1560),
存成倍数是为了缩放时还清楚。

**裁切参数直接读 `CampusData.java`** —— 保证底图和坐标用的是同一个视口。
这两个要是各算各的,每个标记都会偏一点。

---

### 10. `tools/` 里的两个脚本 — 不进提交

| 脚本 | 干什么 |
|---|---|
| `make_campus_data.py` | 读 JSON → 生成 `DKUApp/CampusData.java`,并打印节点/边/门数供对账 |
| `make_campus_map.py` | 读 PDF + 读生成的 `CampusData.java` → 裁出 `DKUApp/campus-map.jpg` |

**为什么单独放一个文件夹、不放进 `DKUApp/`:** 提交物里不该混构建工具。
助教只需要能编译运行,不需要能重新生成数据。

**为什么地图脚本要去读 Java 文件:** 见上面第 9 条,这是防止图和坐标对不上的办法。

---

## 五、一次点击的完整数据流

把文件串起来看一遍,这是理解整个项目最快的路径:

```
① 用户在画布上点了一下
      └─ 触发 CanvasPanel 里注册的 MouseAdapter

② CanvasPanel 把屏幕坐标换算回地图坐标
      └─ mapX(px) / mapY(py)          ← 撤销投影

③ model.clickAt(mapX, mapY)
      └─ Model.nearestSelectable()    ← 只在前 53 个正式节点里找,半径 120 单位内
      └─ 推移选择状态(空 → 起点 → 终点 → 新起点)

④ Model.recomputeRoute()
      └─ Dijkstra.shortestPath(campus, 起点, 终点)

⑤ Dijkstra 在 CampusGraph 上跑
      └─ CampusGraph.roadsFrom(name)  ← 邻接表
      └─ Road.weight                  ← 已经含门费,建图时就算好了
      └─ 返回 List<Node>

⑥ 路线存进 Model

⑦ CanvasPanel 的回调触发 AppFrame.refresh()
      └─ canvas.repaint()             ← 告诉 Swing "该重画了"
      └─ 四个 JLabel 更新文字(getPublicStops 过滤掉路径点)

⑧ Swing 稍后调用 CanvasPanel.paintComponent()
      └─ sx() / sy()                  ← 投影回屏幕
      └─ drawMap → drawEdges → drawRoute → drawNodes
```

---

## 六、关键设计决策速查

| 决策 | 怎么做的 | 为什么 |
|---|---|---|
| **路长从哪来** | 由两点坐标算出来,不手填 | 图片和距离不可能互相矛盾 |
| **门费** | 建图时写进有向边权 `w(u→v) = 距离 + 等效距离(v)` | 算法保持教科书形态,不用为门做特判 |
| **终点免收** | `routeLength()` 里减掉终点那次 | 路线结束在终点,不是穿过它 |
| **坐标系** | 后端保留原始坐标,只有 `CanvasPanel` 做投影 | 后端不碰屏幕,可离线测试 |
| **距离单位** | 后端全程用原始单位,只有 `AppFrame` 打印时乘 `METRES_PER_UNIT` | 同上 —— 换算只发生在"给人看"的那一刻,验证程序一行不用改 |
| **数据存法** | 生成 `.java`,不读外部文件 | 助教解压就能编译 |
| **底图** | 裁切参数读生成的数据文件 | 图和坐标同源 |
| **可点的点** | 只有 53 个正式节点 | 路径点没有名字,点中会显得像 bug |
| **路径点** | 默认不画,复选框打开 | 它们是路线的骨架,但默认会让答案淹没 |
| **算法** | 线性扫描的 Dijkstra | 150 个点,比维护堆简单且够快 |
| **连通性** | 启动时 BFS 检查一遍 | 数据出错时立刻报警,而不是等用户发现算不出路 |

---

## 七、验证过的结论

| 检查 | 结果 |
|---|---|
| Dijkstra vs Bellman-Ford(独立算法,2756 个有序点对) | 0 处不一致 |
| 界面显示的数字 vs 精确值 | 0 处不一致 |
| 生成的数据 vs 原始 JSON(150 坐标 / 207 边 / 门费) | 完全一致,边长误差 ≤ 0.005 |
| 投影不变量 + 点击往返 | 全过 |
| 侧栏只列正式节点 | 9 节点路线 → 只显示 2 个名字 |
| 忽略门费会不会选到更贵的路 | 2756 对里有 1316 对会,最多多付 193 米 |
| 全图连通性 | 150/150 可达 |

测试程序放在 `/tmp/dkutest`(**提交文件夹之外**),包含四个:
`Project`(投影)、`Verify`(算法)、`Panel`(窗口)、`Check_data.py`(数据管线)。
