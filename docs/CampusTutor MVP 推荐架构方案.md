# **CampusTutor (校园智教) O2O家教平台实时推荐系统MVP架构与算法降维落地研究报告**

## **1\. 引言与业务挑战概述**

在当前的数字化服务浪潮中，推荐系统已经从传统的内容分发（如短视频、新闻）向重决策的O2O（Online-to-Offline）服务领域深度渗透 1。以抖音（TikTok）为代表的内容平台，凭借其基于深度学习的实时反馈回路、精细化的流量池赛马机制以及沉浸式的用户体验，构建了极高效率的推荐分发引擎 3。该架构的核心优势在于能够在极短的时间内（通常在几十分钟或数百次交互内）精准捕获用户的兴趣漂移，并通过Monolith等在线学习系统实现模型的准实时更新 3。

然而，将这种内容型推荐机制跨界迁移至O2O家教服务平台“CampusTutor (校园智教)”时，面临着底层逻辑的巨大差异。与短视频的“高频、低决策成本、沉浸式消费”不同，O2O家教匹配呈现出典型的“低频、重决策、强LBS（基于位置的服务）限制、高客单价”特征 5。家教服务涉及高额的时间与金钱投资。例如，在澳大利亚市场，针对高中和专业科目的辅导时薪通常在50至150澳元之间，而初级辅导也需要40至70澳元 7。这种高客单价意味着用户在做出“预约”或“私信”决定前，会进行更为审慎的比较与评估。

此外，当前项目正处于即将上线的纯冷启动阶段。在推荐系统的术语中，冷启动（Cold Start）是指系统在缺乏足够历史交互数据的情况下，无法为新用户或新物品提供准确推荐的困境 9。由于CampusTutor目前仅有模拟数据，传统的协同过滤（Collaborative Filtering）或矩阵分解（Matrix Factorization）模型将因为用户-物品交互矩阵极度稀疏而完全失效 11。

本报告旨在深度拆解抖音推荐算法的核心机制，剥离在O2O场景中产生冗余计算的复杂模型，并针对CampusTutor项目的冷启动阶段，输出一套基于Spring Boot 3与Redis流处理架构的轻量级、可落地、实时反馈的最小可行性产品（MVP）推荐系统架构方案。报告将从特征工程、破冰策略、实时反馈流及算法链路降维四个维度展开详尽论述。

## **2\. 内容推荐与 O2O 服务推荐的底层逻辑分野与降维**

在设计CampusTutor的推荐架构之前，必须深刻理解抖音算法的精髓，并明确哪些机制需要做“减法”（Discard），哪些机制必须进行“改造”（Adapt）。

### **2.1 抖音推荐算法的核心基石**

研究表明，抖音的推荐算法并非依赖某种未知的神秘技术，而是建立在极度优化的工程架构与交互设计之上 13。其核心机制可以归纳为以下几个方面：

首先是极速的实时反馈循环（Real-Time Feedback Loop）。传统的推荐系统大多依赖批处理（Batch Processing），即利用昨天的交互数据在夜间训练模型 3。而抖音的系统专为在线学习设计，用户在一个视频上的停留时间、点赞或滑动行为，会立刻作为特征输入系统，并在毫秒级延迟内改变该会话（Session）中下一个刷出的视频排序 3。

其次是多维度的隐式信号提取。抖音的算法高度依赖完播率（Completion Rate）和观看时长（Watch Time），将其视为比点赞更真实的黄金兴趣信号 3。同时，系统会通过计算机视觉和音频分析技术，提取视频的背景音乐、帧画面、文本标签等多模态特征，将其转化为高维向量（Embeddings）进行相似度计算 3。

最后是严格的流量池赛马机制（Horse Racing Mechanism）。新发布的内容会被投入基础流量池（约500次曝光），如果其互动指标达标，则逐级进入更大的流量池，直至全网爆发；反之则逐渐停止推荐 18。

### **2.2 O2O 场景下的机制“减法”**

相比于短视频的沉浸式消费，家教匹配是典型的目的导向型行为。因此，抖音的诸多机制在CampusTutor中不仅不需要，甚至可能产生负面效果：

第一，沉浸式连续滑动与微秒级多模态特征提取需要被剔除。抖音依赖复杂的深度神经网络来分析视频内容 3。然而，家教平台的核心是结构化数据：教员的履历、所在位置、可授课时间与价格 6。引入高昂的多模态深度特征提取（如分析教员的头像照片特征）在MVP阶段收益极低，且会无谓增加系统的计算延迟与服务器成本 11。

第二，超短周期的完播率模型不适用。抖音将“播放时长”视为核心正反馈 3。在O2O场景中，家长在某位教员详情页停留时间长，可能代表认真阅读并产生意向，但也可能代表对其某些条件（如价格偏高）犹豫不决 21。因此，“停留时长”的预测价值在重决策场景中远不如显式的“点击私信”按钮或“获取联系方式”来得直接。

第三，复杂的重排序（Re-ranking）与多样性打散机制应当被弱化。抖音为了防止用户产生视觉疲劳，会强行混入不同类目的内容（例如连续刷到舞蹈视频后，强制插入科普视频）3。但在明确寻找“12年级HSC高级数学辅导”的家长面前，强制插入一位“小学英语”教员不仅无助于转化，反而会破坏用户的搜索体验并降低平台的专业性。

### **2.3 O2O 场景下的机制“改造与保留”**

在剥离了冗余机制后，CampusTutor必须坚决保留并改造以下抖音的核心机制：

第一，极速的实时反馈循环（Real-Time Feedback Loop）必须保留，但需进行轻量化改造。当家长在浏览教员列表时，如果连续点击了3位“悉尼大学、价格$60/hr、线下授课”的教员详情，系统必须在百毫秒内捕获这一意图，并在下一次下拉刷新（Batch）时，瞬间增加具有类似特征教员的排序权重 23。由于缺乏深度学习模型的算力，这一机制将通过内存数据库（Redis）的有序集合（Sorted Set）来实现。

第二，行为权重的阶梯设定需要重构。抖音将“点赞、评论、转发、完播”赋予不同分值 14。O2O平台需明确界定业务转化漏斗（Conversion Funnel）。在MVP中，应当将“列表曝光”视为无意图，“点击查看详情”视为浅层兴趣（低权重），“收藏教员”视为中度意向，而“发送私信”或“点击预约”视为深度转化意图（极高权重）22。

## **3\. O2O 场景的特征工程：双向标签体系与初始数据库构建**

在推荐系统中，特征工程（Feature Engineering）是决定模型预测能力上限的基础 27。由于项目处于纯冷启动阶段，系统无法从历史交互中学习特征表示，因此必须构建一套极其严密的显式“双向标签库”（Two-way Tagging System），以解决初期的供需信息不对称问题。

### **3.1 标签体系设计原则与空间约束**

O2O家教平台的标签体系需要围绕四个核心维度展开：空间地理位置（LBS Constraints）、知识与学科（Domain Knowledge）、经济期望（Economic Constraints）以及用户基础属性（Demographics）6。

#### **3.1.1 空间与地理位置（LBS）的层级网络**

地理位置是O2O线下服务的第一前置条件。有别于纯在线数字服务，线下家教服务受限于通勤距离、交通成本与物理时间。参考澳大利亚统计局（ABS）的地理行政区划标准（ASGS），LBS标签不能是单一的字符串，而必须构建为多层级的树状结构（Hierarchical Tree Structure），以便于计算空间重叠度 30。

具体的地理标签层级设计如下：

* **一级标签（宏观区域 Region）：** 划分大悉尼地区（Greater Sydney）的宏观板块，如内西区（Inner West）、大西悉尼（Greater Western Sydney）、北岸（North Shore）及中央商务区（CBD）32。这一层级用于在用户未提供精确地址时的泛区域召回。  
* **二级标签（地方政府区域 LGA）：** 例如 City of Parramatta, Cumberland City Council, Sutherland Shire 等 34。LGA是政策与社区服务的基础单元，有助于匹配同属一个行政服务区的供需双方。  
* **三级标签（郊区 Suburb / Locality）：** 具体到如 Strathfield, Epping, Redfern 等具体的居住邮编区 35。这是日常通勤评估最常用的粒度。  
* **微观计算属性（Geo-coordinates）：** 提取精确的经纬度（Latitude/Longitude），利用 Redis GEO 数据结构（如Geohash）进行半径查询，实时计算直线距离或预估通勤时间 37。

#### **3.1.2 经济与价格的区间离散化**

价格是重决策服务中的核心过滤漏斗。在机器学习特征工程中，连续的浮点数价格直接作为特征效果不佳，需要将其离散化为分箱特征（Binned Features）或价格带（Price Bands），以便于算法进行特征交叉与相似度计算 27。

基于2025-2026年澳大利亚辅导市场的真实调研数据，设定以下标准价格标签带 7：

* **基础价格带 ($40 \- $70/hr)：** 通常对应小学（Primary School）全科辅导或低年级基础补习 7。  
* **标准价格带 ($70 \- $100/hr)：** 对应高中（High School）常规科目（如标准英语、基础数学）及一般大学生教员的市场均价 7。  
* **专业/高级价格带 ($100 \- $150+/hr)：** 针对具有极高专业壁垒的需求，如HSC高级数学（Extension 1/2）、物理、化学、VCE/IB备考，或医科入学考试（UCAT），由拥有99.95 ATAR满分的顶尖专家级教员提供 7。

### **3.2 CampusTutor 初始 MVP 标签树定义建议**

基于上述原则，家长（需求方）与教员（服务方）的MVP双向标签树定义与匹配逻辑整合如下表所示：

| 维度类别 | 家长端（需求方）标签提取 | 教员端（服务方）标签提取 | 推荐匹配逻辑与算法权重 |
| :---- | :---- | :---- | :---- |
| **空间位置 (LBS)** | 常驻区域 (LGA/Suburb)、期望授课范围半径、期望模式 (纯线下 / 线上+线下) | 常驻区域 (LGA/Suburb)、最大可接受通勤半径、支持模式 (Online/Offline) | **硬性约束 (Hard Constraint)**。若选择纯线下，距离超出双方最大设定阈值，则在粗排阶段直接被熔断剔除，不进入精排打分。 |
| **知识与学科** | 目标学生年级 (如 Year 1-6, Year 11-12)、薄弱学科 (如 HSC Advanced Math, Physics) | 擅长执教年级区间、精通科目、过往高考ATAR成绩、就读大学与专业 | **强匹配 (Strong Match)**。学科与年级必须完全吻合或处于包含关系中。是候选集生成（Candidate Generation）的第一条件。 |
| **经济预期** | 期望时薪区间 (如 $60-$80/hr)、预算敏感度标签 | 定价水平 (如 $75/hr)、是否支持首次免费试听、是否提供长期打包优惠 (Package) | **意向匹配 (Intent Match)**。价格差异绝对值作为排序损失函数的一部分，偏离预算越远，基础排序得分衰减越严重。 |
| **动态交互行为** | 偏好的教员类型画像（根据会话内的点击历史推导：如持续点击高分学霸，或持续点击女性教员） | 系统记录的响应速度 (Message Response Time)、过往家长评价分数、面试或审核基础评分 | **动态特征 (Dynamic Features)**。随实时交互不断演进，通过Redis实时流处理捕获，触发实时重排（Re-ranking）。 |
| **基础属性** | 注册设备 (iOS/Android)、活跃时间段、新老用户标识 | 性别、可用授课时间表 (Availability Calendar)、教龄 | 弱匹配。仅作最终得分的微调。时间表冲突直接作降权处理。 |

在纯冷启动阶段，系统完全没有任何动态交互行为数据，此时推荐系统将高度依赖注册时填写的这套结构化知识标签进行初始召回（Recall），将其作为构建用户画像和破冰推荐的唯一基石 9。

## **4\. 纯冷启动破冰策略：无历史数据下的初始流量分配**

在推荐系统的生命周期中，新平台上线面临的是“全局冷启动（Global Cold Start）”问题，即系统中既没有用户的历史行为，也没有物品（教员）的被消费记录 10。在这种极度缺乏信息的“盲视”状态下，盲目部署预测模型只会产生随机噪音。因此，必须通过产品设计与硬性策略的结合来强行破冰。

### **4.1 家长端（新用户）的兜底推荐策略**

当新注册的家长首次打开小程序，系统对其偏好一无所知。如果采用“展现全平台最受欢迎教员”的传统做法，会导致严重的地域错配（如将悉尼东区的名师推荐给西区的家长）。因此，建议采用以下结构化的混合兜底策略（Hybrid Fall-back Strategy）9：

#### **4.1.1 强引导式交互微调查（Active Learning / Onboarding Survey）**

解决用户冷启动最有效的方法是将其转化为主动学习过程 28。在新用户注册或首次进入首页时，系统应设计一个极简的交互卡片，强制或半强制地要求家长选择核心需求。例如：

1. “您的孩子目前就读几年级？”  
2. “主要需要提升哪些科目？”  
3. “您倾向于面对面线下辅导，还是线上视频辅导？”  
4. “请授权获取您的当前位置或输入您所在的 Suburb。”

这种主动收集的显式反馈（Explicit Feedback）能够瞬间提供构建用户初始画像所需的全部高权重特征，极大地缓解数据稀疏性问题 6。

#### **4.1.2 基于知识与规则的强匹配（Knowledge-based Fallback）**

在获取了初始问卷数据后，首屏的推荐引擎不再调用任何复杂的预测算法，而是退化为基于规则的数据库检索引擎（Rule-based Retrieval）9。系统在底层执行硬性条件的交集查询： 即：只召回满足 \[指定学科\] \+ \[涵盖指定年级\] \+ \[距离限制在X公里以内或提供线上服务\] 的教员 9。这一策略确保了推荐内容具备最基本的可用性与逻辑合理性，防止用户在首次体验时流失（Churn）20。

#### **4.1.3 LBS距离优先与高斯白噪声探索（Exploration & Exploitation）**

在上述规则生成的候选池中，由于缺乏互动评分，排序（Ranking）应优先遵循物理世界的逻辑：按照 LBS 距离由近及远进行粗排展示。

然而，仅依赖距离会陷入“探索与利用（E\&E）”困境中的过度利用陷阱，导致过滤气泡。为了测试家长的隐式价格弹性与偏好边界，平台应当在最初的30个推荐结果中，人工注入部分带有“高斯白噪声（Gaussian Noise）”属性的探索性样本 3。 例如，系统可以在距离较近的普通教员列表中，强行插入2-3个距离稍远但评价极高、或是价格显著超出预期但拥有满分ATAR成绩的明星教员。如果家长对这些“越界”的教员产生了点击甚至私信，系统就能立即判断出该家长对“教学质量”的敏感度远高于对“距离和价格”的敏感度，从而为后续的实时个性化排序提供宝贵的锚点数据。

### **4.2 教员端（新物品）的初始流量分配与赛马机制**

在O2O双边市场中，教员是平台的核心资产（Inventory）。新入驻的教员如果长时间得不到曝光和成单，将导致供给端迅速萎缩。抖音解决新视频冷启动的核心武器是“多级流量池与赛马机制（Traffic Pool Horse Racing Mechanism）” 18。CampusTutor 可以提取其精髓，摒弃完播率指标，设计一套适配家教业务的阶梯式流量爬升体系。

#### **4.2.1 O2O化赛马漏斗模型重构**

在抖音的机制中，视频发布后会先进入约500人规模的基础流量池，通过点赞、评论和完播率的综合得分决定是否晋级到更大的流量池 18。在CampusTutor中，由于决策链条长，核心赛马指标必须转化为业务漏斗的转化率：即“曝光 \-\> 详情页点击率（CTR）”和“详情页 \-\> 私信/预约发起转化率（CVR）” 21。

具体赛马层级设计与实施步骤如下：

* **Level 1：基础冷启流量池 (Basic Pool \- 探索期)**  
  * *触发条件：* 新教员完成注册、身份认证与资料完善审核。  
  * *分配策略：* 平台通过流量干预，在接下来的7天内，给予该教员约 200 \- 500 次的强制优先曝光（Impressions）。这些曝光并非随机投放，而是精准定向给其 LBS 覆盖范围内、且正好搜索相关学科的家长 18。前端可通过打上“新晋优秀教员”或“平台严选”标签来吸引点击。  
  * *晋级考核指标：* 考核其列表卡片的详情页点击率（CTR）是否达到基准线（例如行业平均 5%）。达到基准线说明其头像、学校背景和报价在市场中具备初步吸引力。  
* **Level 2：温水放大流量池 (Warm Pool \- 验证期)**  
  * *触发条件：* 在基础池中 CTR 达标，或获得了首次有效的家长“发送私信”。  
  * *分配策略：* 系统解除其部分硬性限制，例如将其推荐的 LBS 曝光半径从 5km 扩大至 15km，同时在类目聚合页（如“高中HSC数学教员推荐专区”）赋予更高的基准排序权重（Base Score）。  
  * *晋级考核指标：* 核心考核私信转化率（CVR）与最终试听成功率 26。此外，平台应引入服务质量指标：如果家长发送私信后，该教员的响应回复速度（Message Response Time）极快，系统应在算法中给予显著的权重加分 43。  
* **Level 3：热门核心流量池 (Hot Pool \- 爆发期)**  
  * *触发条件：* 产生多次实质交易、收获家长正向评价（5星好评），且综合 CTR 与 CVR 指标位居前列。  
  * *分配策略：* 为其打上“明星教员”、“近期热门”等权威标识，将其作为平台首页的默认兜底推荐内容展示给所有新访客。对于寻求纯线上辅导（Online Tutoring）的用户，打破所有地理限制进行全州甚至全国范围的曝光 18。  
  * *退出与降级机制（出局）：* 推荐系统必须保证流动性。当某个核心教员的近期点击数据停滞、拒绝接单次数过多、长时间不回复信息或评分出现下降趋势时，算法时间衰减机制将逐步削弱其推荐权重，将其平滑移出核心流量池，为新教员腾出空间 19。

通过这套机制，平台能够在无需过度依赖人工运营干预的情况下，依靠市场（家长端）的真实点击和私信行为，自动、持续地筛选并置顶最优质的大学生教员，形成良性的平台生态。

## **5\. 召回与排序的算法链路降维：构建极简架构**

在处理完冷启动破冰后，系统需要进入常态化的推荐分发流程。现代工业级推荐系统通常采用“召回（Candidate Generation / Retrieval） \-\> 粗排 \-\> 精排（Ranking） \-\> 重排（Re-ranking）”的漏斗式多阶段架构 11。对于千万级日活的平台，这一漏斗可能涉及数十个深度学习模型。

然而，在研发成本受限且追求快速上线的MVP阶段，过度复杂的架构是灾难性的。我们需要为CampusTutor设计一条极致精简、重业务逻辑、轻模型运算的算法链路 11。

### **5.1 第一阶段：多路召回 (Candidate Generation)**

召回阶段的任务是从全库数以万计的教员中，以极低的延迟（通常数十毫秒）筛选出几百名可能符合用户需求的候选人 11。我们采用双通道多路召回策略：

1. **LBS 空间召回通道：** 不使用复杂的空间聚类算法，而是直接依赖 Redis 内置的 GEO 数据结构。将所有教员的经纬度存储在 Redis 中，当家长发起请求时，使用 GEOSEARCH 或 GEORADIUS 指令，瞬间召回以家长坐标为圆心、指定公里数（如 10km）为半径内的所有教员ID集合 37。  
2. **硬性标签倒排召回通道：**  
   利用 Elasticsearch 或关系型数据库的倒排索引（Inverted Index），基于家长填写的“科目”与“年级”进行精确匹配，召回另一个教员ID集合。

将上述两个通道召回的集合取交集（Intersection），即得到了满足物理距离且学科匹配的“高质量初始候选集池（Candidate Pool）”。

### **5.2 第二阶段：业务规则过滤 (Rule-based Filtering)**

获取候选集后，必须接入硬性的业务规则引擎过滤无效数据，以避免浪费后续的算力：

* **状态过滤：** 剔除当前标记为“已满负荷（Fully Booked）”、“请假中”或“账号异常/被封禁”的教员。  
* **黑名单过滤：** 剔除家长曾经拉黑或明确点击过“不感兴趣”的教员。  
* **防骚扰控频：** 如果某教员在近一小时内已被系统推给了大量用户且收到了海量私信，系统可触发熔断机制，暂时将其移出候选集，避免该教员不堪重负。

### **5.3 第三阶段：基于轻量级意图打分的精排 (Lightweight Fine-Ranking)**

这是体现“越刷越懂我”核心体验的关键环节。在剔除无效教员后，剩下的数百名候选教员将被送入精排引擎。

在这一步，我们放弃了抖音使用的复杂逻辑回归（LR）、XGBoost或双塔深度神经网络（Two-Tower DNN），因为这些模型需要大量的历史训练样本与线上特征存储（Feature Store）支持 11。

取而代之的是，采用一套**基于时间的启发式规则算分引擎（Heuristic Time-Decayed Scoring Engine）**。系统将提取每一个候选教员的基础静态分（如历史转化率、评价星级），再叠加上当前家长在**本次浏览会话（Session）中产生的实时意图动态分**，两者加权求和，得出最终的排序分数（Final Ranking Score），按分数降序排列后截取前 N 名（Batch Size）返回给前端展示 23。

具体的分数计算与实时捕获流技术，将在下一章节详细展开。

## **6\. 轻量级实时反馈流：核心难点与架构技术实现**

实现抖音式体验的核心难点在于“实时计算”——即家长一旦发生“点击详情”或“发送私信”行为，系统必须在秒级延迟内捕获这些特征，并在下一次滚动刷新时立刻改变推荐流的排序 3。

考虑到研发成本，本架构采用 **Spring Boot 3 \+ Redis** 作为底层基础设施，巧妙利用 Redis 的轻量级消息队列（Streams）和高性能有序集合（Sorted Set / ZSET），构建一套零模型依赖的纯内存实时意图打分引擎 20。

### **6.1 实时数据流转微服务架构**

整个实时反馈系统的生命周期分为前端采集、事件总线、异步流处理与实时排序分发四个模块：

1. **事件采集接入层 (Ingestion Layer)：** 部署在 Spring Boot 3 中的轻量级网关。当家长在小程序端触发“点击查看教员详情”或“点击发送私信”时，前端非阻塞地向后端发送一个极简的 JSON 事件，包含 userId (家长ID), tutorId (被交互的教员ID), actionType (动作类型) 和 timestamp (时间戳) 46。  
2. **事件总线 (Event Bus \- Redis Streams)：** 为了避免引入 Kafka 带来的高昂运维成本，Spring Boot 接收到事件后，使用 RedisTemplate 将事件快速推入 Redis Streams（利用 XADD 指令）。Redis Streams 作为一种持久化、支持消费者组（Consumer Groups）的有序消息队列，完美胜任此类中等规模的高吞吐流处理，且能够保证事件的有序性与不丢失 46。  
3. **异步流处理与意图打分层 (Processing Layer)：** 后端的微服务后台线程作为消费者（Consumer），实时从 Redis Streams 中拉取数据（利用 XREADGROUP）。读取到事件后，程序会查询目标教员身上的核心标签（如：该教员带有 “价格$60/hr”、“悉尼大学”、“女性”等特征），然后根据动作的权重，将这些偏好分值增量地累加到该家长专属的 Redis 有序集合（ZSET）中，以此来追踪家长的实时意图漂移 47。  
4. **推荐服务分发层 (Serving Layer)：** 当家长继续向下滑动触发下一页的分页请求时，推荐接口直接从该家长对应的 Redis ZSET 中执行 ZREVRANGE 操作，提取当前得分最高的几个意图标签。然后将这些高权重意图带入前文提到的“精排引擎”中对候选教员进行重新算分，最后将重排后的结果返回前端 47。整个流程纯基于内存操作，响应延迟可控制在几十毫秒级以内，完全满足实时性要求 20。

### **6.2 轻量级实时意图打分公式 (Time-Decayed Intent Scoring)**

如何将用户的每一次点击科学地转化为对标签偏好的量化？由于用户的兴趣在浏览过程中是动态变化的（例如刚开始看重价格，看了一圈后可能转向看重学历），我们需要引入**指数时间衰减模型（Exponential Time Decay Model）** 45。10秒前点击了一个“新南威尔士大学”的教员，比10分钟前点击的同类教员更有指向价值。

#### **6.2.1 交互动作的基础权重分配表**

首先，为不同的核心交互指标赋予阶梯式的静态基础权重（![][image1]）14：

| 交互行为类别 (action) | 业务漏斗含义 | MVP基础权重设定 (Waction​) |
| :---- | :---- | :---- |
| **浏览列表曝光 (Impression)** | 浅层略过，无正向意图，可作为轻微负向信号防频繁推荐 | 0 或 \-0.1 |
| **点击查看详情 (Click Detail)** | 产生初步兴趣，愿意投入时间查看履历细节 | \+ 1.0 |
| **收藏/关注教员 (Save/Follow)** | 强烈的意向储备，但尚未下定决心破冰交流 | \+ 3.0 |
| **点击发送私信 (Send Message)** | 极强的即时转化意图，核心业务指标 | **\+ 5.0** |

#### **6.2.2 指数衰减实时偏好得分公式定义**

设在当前的会话窗口（例如最近 30 分钟）内，家长 ![][image2] 对带有某种特征标签 ![][image3]（例如：“时薪$60/hr”、“USYD”、“擅长数学”）的一系列教员产生了一组操作动作集合 ![][image4]。

那么，家长 ![][image2] 在当前时刻 ![][image5] 对于标签 ![][image3] 的实时偏好得分 ![][image6] 定义为物理学中的指数衰减公式 45：

![][image7]  
其中：

* ![][image8]：第 ![][image9] 次动作发生时的基础权重（如发送私信 ![][image10]）。  
* ![][image11]：第 ![][image9] 次动作发生时的确切时间戳。  
* ![][image12]：动作发生至今流逝的时间差（![][image13]，以秒或分钟为单位）。  
* ![][image14]：指数衰减常数（Decay Constant）45。![][image14] 的取值取决于业务对“兴趣遗忘速度”的定义（半衰期 Half-life）。例如，假设业务认为家长在一个Session中的兴趣半衰期是 5 分钟（即5分钟前的点击现在的价值只剩一半），则根据半衰期公式推导 ![][image15]。

#### **6.2.3 候选教员的最终精排得分机制 (Final Ranking Score)**

当系统准备输出下一批推荐名单时，遍历召回池中的每一个候选教员 ![][image16]。基于该教员自身拥有的标签集合 ![][image17]，计算其特征与当前家长衰减后的实时意图的重合度。最终排序总分 ![][image18] 表示为 23：

![][image19]

* ![][image20]：教员的全局静态基础分（由完课率、好评率等决定）。  
* ![][image21] 和 ![][image22]：系统超参数。在纯冷启动无交互时，![][image22] 产生的增量得分为 0，系统完全依赖基础分数进行兜底规则排序；一旦家长开始产生持续点击，动态累加的实时偏好得分 ![][image23] 迅速膨胀，![][image22] 权重介入，瞬间将包含命中标签（如刚刚频繁点击的特定价格带或特定大学标签）的教员顶至列表最前方 24。

### **6.3 Spring Boot \+ Redis 核心伪代码逻辑落地**

为了在工程上避免每次刷新都进行繁重的浮点数指数运算，我们在Redis流处理中使用增量更新逻辑，利用 Redis Sorted Set (ZSET) 原生支持排名的特性来维护家长的意图榜单 47。

Java

/\*\*  
 \* 模块一：异步流处理器 (监听 Redis Streams)  
 \* 当家长点击或私信某个教员时触发  
 \*/  
public void handleUserRealTimeAction(String parentId, String tutorId, String actionType) {  
      
    // 1\. 获取业务动作对应的基础权重  
    double weight \= getActionWeight(actionType); // 比如私信 weight \= 5.0，点击 \= 1.0  
      
    // 2\. 获取被操作目标教员身上的核心属性标签 (假设在Redis Hash中提前静态缓存)  
    // 标签例如:  
    List\<String\> tags \= redisTemplate.opsForHash().values("tutor\_static\_tags:" \+ tutorId);  
      
    // 3\. 增量更新该家长当前Session的实时标签偏好分 (写入 Redis ZSET)  
    String sessionIntentKey \= "realtime\_intent:parent:" \+ parentId;  
    for (String tag : tags) {  
        // 利用 Redis ZINCRBY 命令原子性操作，直接为该标签增加权重分  
        redisTemplate.opsForZSet().incrementScore(sessionIntentKey, tag, weight);  
    }  
      
    // 4\. 设定该实时意图追踪缓存的物理过期时间   
    // (如30分钟无操作后整个会话过期清零，作为宏观的暴力衰减兜底)  
    redisTemplate.expire(sessionIntentKey, 30, TimeUnit.MINUTES);  
}

/\*\*  
 \* 模块二：推荐请求引擎 (处理前端下拉刷新请求)  
 \* 推荐下一批次教员时的精排逻辑  
 \*/  
public List\<Tutor\> getNextBatchRecommendations(String parentId, List\<Tutor\> candidateTutors) {  
      
    String sessionIntentKey \= "realtime\_intent:parent:" \+ parentId;  
      
    // 1\. 获取家长当前最高权重的Top 5意图标签及其累加分数  
    Set\<ZSetOperations.TypedTuple\<String\>\> topIntentTags \=   
        redisTemplate.opsForZSet().reverseRangeWithScores(sessionIntentKey, 0, 4);  
      
    // 2\. 遍历所有初筛候选教员，计算综合得分  
    for (Tutor candidate : candidateTutors) {  
        double finalScore \= candidate.getBaseStaticScore(); // 获取教员基础权重分 (alpha 项)  
          
        if (topIntentTags\!= null &&\!topIntentTags.isEmpty()) {  
            for (TypedTuple\<String\> intentTag : topIntentTags) {  
                // 如果该候选教员身上带有家长刚刚高频交互过的标签，则获得实时意图加分！  
                if (candidate.hasTag(intentTag.getValue())) {  
                    // 近似时间衰减计算: 在请求时根据预存的时间戳计算衰减后的实际分数  
                    // (此处为保持伪代码简洁，略去获取具体上次交互时间的逻辑)  
                    double rawScore \= intentTag.getScore();  
                    double decayedScore \= applyExponentialDecay(rawScore, timeElapsed);  
                      
                    // beta 放大因子项  
                    finalScore \+= (decayedScore \* 1.5);   
                }  
            }  
        }  
        candidate.setCurrentRankingScore(finalScore); // 注入计算结果  
    }  
      
    // 3\. 按照计算后的最终分数进行降序排列  
    candidateTutors.sort(Comparator.comparingDouble(Tutor::getCurrentRankingScore).reversed());  
      
    // 4\. 截断并返回给前端展示 (如返回前 10 个教员)  
    return candidateTutors.subList(0, Math.min(BATCH\_SIZE, candidateTutors.size()));  
}

这套技术实现的精妙之处在于：摒弃了离线的大数据数仓计算（如Hadoop/Spark），直接在热数据内存区（Redis ZSET）完成“特征记录与算分” 46。只要家长产生交互，特征分数立刻改变；当用户滑动手指进入下一屏时，后端的算法只需执行极低开销的内存匹配和加法运算，从而在微观工程层面实现了肉眼可见的“越刷越懂我”的实时反馈闭环体验 3。

## **7\. 结语与后续演进建议**

对于 CampusTutor 这一类低频、重决策、受物理空间强烈约束的 O2O 双边服务平台而言，在产品初期（纯冷启动阶段）强行部署类似抖音的重型多模态深度学习推荐模型，不仅会带来难以承受的研发与服务器成本，而且由于底层业务逻辑的分野，极易产生“南辕北辙”的推荐灾难。

通过深度的算法抽象、降维与提纯，本报告构建了一套完全依托 Spring Boot 3 与 Redis 架构的最小可行性推荐系统（MVP）。其战略核心在于三点： 首先，坚持明确的硬性边界。将 LBS 空间层级约束与学科年级要求作为雷打不动的底层过滤网，确保推荐结果具备实际履约的物理意义与教育意义 29。 其次，通过产品设计盘活冷启动流量。利用强制微调查（Onboarding Survey）打破初始的信息迷雾，并通过重新定义的阶梯式赛马漏斗机制，在基础曝光、验证点击、转化私信的进程中，自发、持续地从供给端筛选出优质的大学生教员 18。 最后，以轻巧的技术架构实现敏捷响应。运用 Redis Streams 事件流处理及 ZSET 有序集合，结合经典的指数时间衰减意图打分公式，成功用极低的算力开销实现了对家长行为的秒级捕捉与列表重排序，保留了抖音算法中最具商业价值的“实时反馈回路” 45。

随着平台后续日活的不断增长与历史数据的丰满沉淀，该 MVP 架构可无缝过渡到更为复杂的工业级机器学习体系。未来可在该流程的重排序（Re-ranking）阶段引入 XGBoost 或轻量级双塔模型（Two-Tower Model）替代启发式规则，但在当前及上线初期的关键破冰阶段，这套降维后的高敏捷、低延迟架构足以支撑业务的高效验证与高速增长。

#### **引用的著作**

1. A personalized point-of-interest recommendation system for O2O commerce, 访问时间为 三月 10, 2026， [https://ideas.repec.org/a/spr/elmark/v31y2021i2d10.1007\_s12525-020-00416-5.html](https://ideas.repec.org/a/spr/elmark/v31y2021i2d10.1007_s12525-020-00416-5.html)  
2. Recommender system \- Wikipedia, 访问时间为 三月 10, 2026， [https://en.wikipedia.org/wiki/Recommender\_system](https://en.wikipedia.org/wiki/Recommender_system)  
3. TikTok's Algorithm Explained Like You're 5 (Because the Official ..., 访问时间为 三月 10, 2026， [https://ai.plainenglish.io/tiktoks-algorithm-explained-like-you-re-5-because-the-official-docs-explain-it-like-you-re-50-0c02479be44f](https://ai.plainenglish.io/tiktoks-algorithm-explained-like-you-re-5-because-the-official-docs-explain-it-like-you-re-50-0c02479be44f)  
4. The Secret Sauce of Tik-Tok's Recommendations \- Shaped, 访问时间为 三月 10, 2026， [https://www.shaped.ai/blog/the-secret-sauce-of-tik-toks-recommendations](https://www.shaped.ai/blog/the-secret-sauce-of-tik-toks-recommendations)  
5. An overview of video recommender systems: state-of-the-art and research issues \- PMC, 访问时间为 三月 10, 2026， [https://pmc.ncbi.nlm.nih.gov/articles/PMC10642507/](https://pmc.ncbi.nlm.nih.gov/articles/PMC10642507/)  
6. Recommendation Engines: How They Work and Why they Matter | Aerospike, 访问时间为 三月 10, 2026， [https://aerospike.com/blog/recommendation-engines-how-they-work/](https://aerospike.com/blog/recommendation-engines-how-they-work/)  
7. How Much Should I Pay for Private Tutoring in 2025?, 访问时间为 三月 10, 2026， [https://www.tutoringlounge.com.au/how-much-should-i-pay-for-private-tutoring-in-2025/](https://www.tutoringlounge.com.au/how-much-should-i-pay-for-private-tutoring-in-2025/)  
8. How Much Do Tutors Cost in Australia 2026? \- KIS Academics, 访问时间为 三月 10, 2026， [https://kisacademics.com/blog/how-much-do-tutors-cost-in-australia-2025-guide/](https://kisacademics.com/blog/how-much-do-tutors-cost-in-australia-2025-guide/)  
9. How to solve the cold start problem in recommender systems ..., 访问时间为 三月 10, 2026， [https://thingsolver.com/blog/the-cold-start-problem/](https://thingsolver.com/blog/the-cold-start-problem/)  
10. Cracking the Cold Start Problem in Recommender Systems: A Practitioner's Guide | by Loli Tep | Data Scientist's Handbook | Medium, 访问时间为 三月 10, 2026， [https://medium.com/data-scientists-handbook/cracking-the-cold-start-problem-in-recommender-systems-a-practitioners-guide-069bfda2b800](https://medium.com/data-scientists-handbook/cracking-the-cold-start-problem-in-recommender-systems-a-practitioners-guide-069bfda2b800)  
11. One-Stop Guide for Production Recommendation Systems | by Zain ul Abideen | Medium, 访问时间为 三月 10, 2026， [https://medium.com/@zaiinn440/one-stop-guide-for-production-recommendation-systems-9491f68d92e3](https://medium.com/@zaiinn440/one-stop-guide-for-production-recommendation-systems-9491f68d92e3)  
12. Preference-Tree-Based Real-Time Recommendation System \- PMC, 访问时间为 三月 10, 2026， [https://pmc.ncbi.nlm.nih.gov/articles/PMC9030273/](https://pmc.ncbi.nlm.nih.gov/articles/PMC9030273/)  
13. TikTok's Secret Sauce \- | Knight First Amendment Institute, 访问时间为 三月 10, 2026， [https://knightcolumbia.org/blog/tiktoks-secret-sauce](https://knightcolumbia.org/blog/tiktoks-secret-sauce)  
14. Mastering the TikTok Algorithm: Understanding the Point System \- Blog, 访问时间为 三月 10, 2026， [https://blog.ocoya.com/blog/mastering-tiktok-algorithm](https://blog.ocoya.com/blog/mastering-tiktok-algorithm)  
15. Decoding the Algorithm: The Mathematics Behind TikTok's Short-Form Content Success \- Digital Commons @ UConn \- University of Connecticut, 访问时间为 三月 10, 2026， [https://digitalcommons.lib.uconn.edu/cgi/viewcontent.cgi?article=2104\&context=srhonors\_theses](https://digitalcommons.lib.uconn.edu/cgi/viewcontent.cgi?article=2104&context=srhonors_theses)  
16. How does the TikTok algorithm work in 2025? Tips to boost visibility \- Hootsuite Blog, 访问时间为 三月 10, 2026， [https://blog.hootsuite.com/tiktok-algorithm/](https://blog.hootsuite.com/tiktok-algorithm/)  
17. Automatic Movie Tag Generation System for Improving the Recommendation System \- MDPI, 访问时间为 三月 10, 2026， [https://www.mdpi.com/2076-3417/12/21/10777](https://www.mdpi.com/2076-3417/12/21/10777)  
18. Understanding Traffic \- TikTok Shop Seller Center, 访问时间为 三月 10, 2026， [https://seller-us.tiktok.com/university/course?learning\_id=2852280135698218\&content\_id=2410858052077355\&lang=en](https://seller-us.tiktok.com/university/course?learning_id=2852280135698218&content_id=2410858052077355&lang=en)  
19. Study on the Construction of Paid Traffic Test Model for Tiktok Car Account, 访问时间为 三月 10, 2026， [https://drpress.org/ojs/index.php/ijeh/article/download/23373/22924/30598](https://drpress.org/ojs/index.php/ijeh/article/download/23373/22924/30598)  
20. AI Recommendation Systems: Fast Real-Time Infrastructure Guide 2026, 访问时间为 三月 10, 2026， [https://redis.io/blog/real-time-ai-recommendation-systems/](https://redis.io/blog/real-time-ai-recommendation-systems/)  
21. A Comprehensive Survey of Evaluation Techniques for Recommendation Systems \- arXiv, 访问时间为 三月 10, 2026， [https://arxiv.org/html/2312.16015v2](https://arxiv.org/html/2312.16015v2)  
22. Click to Message Ads: 4 Best Practices to Maximize Leads \- Respond.io, 访问时间为 三月 10, 2026， [https://respond.io/blog/click-to-message-ads](https://respond.io/blog/click-to-message-ads)  
23. Real-time Retrieval for Recommendations \- ApplyingML, 访问时间为 三月 10, 2026， [https://applyingml.com/resources/real-time-recommendations/](https://applyingml.com/resources/real-time-recommendations/)  
24. Building a real-time recommendation system | by Rishav Ray | Vector Database for AI, 访问时间为 三月 10, 2026， [https://medium.com/vector-database/building-a-real-time-recommendation-system-722d8b987b78](https://medium.com/vector-database/building-a-real-time-recommendation-system-722d8b987b78)  
25. TikTok Algorithm 2026: How the FYP Really Works (Ultimate Guide) \- Beats To Rap On, 访问时间为 三月 10, 2026， [https://beatstorapon.com/blog/tiktok-algorithm-the-ultimate-guide/](https://beatstorapon.com/blog/tiktok-algorithm-the-ultimate-guide/)  
26. The relevance of lead prioritization: a B2B lead scoring model based on machine learning, 访问时间为 三月 10, 2026， [https://www.frontiersin.org/journals/artificial-intelligence/articles/10.3389/frai.2025.1554325/full](https://www.frontiersin.org/journals/artificial-intelligence/articles/10.3389/frai.2025.1554325/full)  
27. Feature Engineering for Recommendation Systems – Part 1 \- AI Infrastructure Alliance, 访问时间为 三月 10, 2026， [https://ai-infrastructure.org/feature-engineering-for-recommendation-systems-part-1/](https://ai-infrastructure.org/feature-engineering-for-recommendation-systems-part-1/)  
28. 7 Proven Strategies to Beat the Cold Start Problem in Recommender Systems, 访问时间为 三月 10, 2026， [https://www.crestechsoftware.com/how-recommender-system-learns-from-zero/](https://www.crestechsoftware.com/how-recommender-system-learns-from-zero/)  
29. SocialRec: User Activity Based Post Weighted Dynamic Personalized Post Recommendation System in Social Media \- arXiv, 访问时间为 三月 10, 2026， [https://arxiv.org/html/2407.09747v1](https://arxiv.org/html/2407.09747v1)  
30. Geotagging and CLASS Reporting \- Community Legal Centres Australia, 访问时间为 三月 10, 2026， [https://clcs.org.au/services/class/reporting-and-accountability/geocoding-and-administrative-boundaries/geotagging-and-class-reporting/](https://clcs.org.au/services/class/reporting-and-accountability/geocoding-and-administrative-boundaries/geotagging-and-class-reporting/)  
31. Local Government Areas | Australian Bureau of Statistics, 访问时间为 三月 10, 2026， [https://www.abs.gov.au/statistics/standards/australian-statistical-geography-standard-asgs-edition-3/jul2021-jun2026/non-abs-structures/local-government-areas](https://www.abs.gov.au/statistics/standards/australian-statistical-geography-standard-asgs-edition-3/jul2021-jun2026/non-abs-structures/local-government-areas)  
32. Regions of Sydney \- Wikipedia, 访问时间为 三月 10, 2026， [https://en.wikipedia.org/wiki/Regions\_of\_Sydney](https://en.wikipedia.org/wiki/Regions_of_Sydney)  
33. How to categorise local areas in Australia \- ID (Informed Decisions), 访问时间为 三月 10, 2026， [https://www.id.com.au/insights/articles/how-to-categorise-local-areas-in-australia/](https://www.id.com.au/insights/articles/how-to-categorise-local-areas-in-australia/)  
34. Local government areas of New South Wales \- Wikipedia, 访问时间为 三月 10, 2026， [https://en.wikipedia.org/wiki/Local\_government\_areas\_of\_New\_South\_Wales](https://en.wikipedia.org/wiki/Local_government_areas_of_New_South_Wales)  
35. Complete Official List of All Sydney Suburbs, 访问时间为 三月 10, 2026， [https://www.walksydneystreets.net/suburbssydneyall.htm](https://www.walksydneystreets.net/suburbssydneyall.htm)  
36. City of Sydney \- Wikipedia, 访问时间为 三月 10, 2026， [https://en.wikipedia.org/wiki/City\_of\_Sydney](https://en.wikipedia.org/wiki/City_of_Sydney)  
37. How To Build a Real-Time Product Recommendation System Using Redis and DocArray, 访问时间为 三月 10, 2026， [https://redis.io/blog/real-time-product-recommendation-docarray/](https://redis.io/blog/real-time-product-recommendation-docarray/)  
38. What does private tutoring cost in 2026? \- Learnmate., 访问时间为 三月 10, 2026， [https://learnmate.com.au/how-much-should-i-pay-for-private-tutoring/](https://learnmate.com.au/how-much-should-i-pay-for-private-tutoring/)  
39. A Comparative Study on Recommendation Algorithms: Online and Offline Evaluations on a Large-scale Recommender System \- arXiv.org, 访问时间为 三月 10, 2026， [https://arxiv.org/html/2411.01354v1](https://arxiv.org/html/2411.01354v1)  
40. How we solve the “cold start problem” in an ML recommendation system \- Reddit, 访问时间为 三月 10, 2026， [https://www.reddit.com/r/ProductManagement/comments/1j5rss9/how\_we\_solve\_the\_cold\_start\_problem\_in\_an\_ml/](https://www.reddit.com/r/ProductManagement/comments/1j5rss9/how_we_solve_the_cold_start_problem_in_an_ml/)  
41. \[2210.09672\] Addressing the Extreme Cold-Start Problem in Group Recommendation \- arXiv.org, 访问时间为 三月 10, 2026， [https://arxiv.org/abs/2210.09672](https://arxiv.org/abs/2210.09672)  
42. How TikTok's Algorithm Decides What You See (Reverse-Engineered) | by Sohail Saifi, 访问时间为 三月 10, 2026， [https://medium.com/@sohail\_saifi/how-tiktoks-algorithm-decides-what-you-see-reverse-engineered-19bf47e66bf4](https://medium.com/@sohail_saifi/how-tiktoks-algorithm-decides-what-you-see-reverse-engineered-19bf47e66bf4)  
43. 5 lead scoring examples for better lead management \- LeadsBridge, 访问时间为 三月 10, 2026， [https://leadsbridge.com/blog/lead-scoring-examples/](https://leadsbridge.com/blog/lead-scoring-examples/)  
44. A Practical Guide to Building an Online Recommendation System \- Databricks, 访问时间为 三月 10, 2026， [https://www.databricks.com/blog/guide-to-building-online-recommendation-system](https://www.databricks.com/blog/guide-to-building-online-recommendation-system)  
45. Using Time Decay in Predictive Lead Scoring | by Filip Vozarevic | Medium, 访问时间为 三月 10, 2026， [https://medium.com/@filip.vozarevic/using-time-decay-in-predictive-lead-scoring-852de2052ea](https://medium.com/@filip.vozarevic/using-time-decay-in-predictive-lead-scoring-852de2052ea)  
46. Real-time Data Ingestion and Delivery using Spring Boot and Redis Streams: A Beginner Friendly Case Study | by Jayanthpawar | Medium, 访问时间为 三月 10, 2026， [https://medium.com/@jayanthpawar18/real-time-data-ingestion-and-delivery-using-spring-boot-and-redis-streams-a-beginner-friendly-case-d8775fb2aace](https://medium.com/@jayanthpawar18/real-time-data-ingestion-and-delivery-using-spring-boot-and-redis-streams-a-beginner-friendly-case-d8775fb2aace)  
47. Build a Real-Time Leaderboard with Redis Sorted Sets, 访问时间为 三月 10, 2026， [https://redis.io/tutorials/howtos/leaderboard/](https://redis.io/tutorials/howtos/leaderboard/)  
48. Redis Sorted Sets, 访问时间为 三月 10, 2026， [https://redis.io/glossary/redis-sorted-sets/](https://redis.io/glossary/redis-sorted-sets/)  
49. Event-Driven Architecture With Redis Streams Using Spring Boot | by Eresh Gorantla | Nerd For Tech | Medium, 访问时间为 三月 10, 2026， [https://medium.com/nerd-for-tech/event-driven-architecture-with-redis-streams-using-spring-boot-a81a1c9a4cde](https://medium.com/nerd-for-tech/event-driven-architecture-with-redis-streams-using-spring-boot-a81a1c9a4cde)  
50. pilotpirxie/recommendation-redis: Simple and open source recommendation system powered by Redis \- GitHub, 访问时间为 三月 10, 2026， [https://github.com/pilotpirxie/recommendation-redis](https://github.com/pilotpirxie/recommendation-redis)  
51. Collaborative filtering: How to build a recommender system \- Redis, 访问时间为 三月 10, 2026， [https://redis.io/blog/collaborative-filtering-how-to-build-a-recommender-system/](https://redis.io/blog/collaborative-filtering-how-to-build-a-recommender-system/)  
52. Exponential Decay Function-Based Time-Aware Recommender System for E-Commerce Applications \- The Science and Information (SAI) Organization, 访问时间为 三月 10, 2026， [https://thesai.org/Downloads/Volume13No10/Paper\_71-Exponential\_Decay\_Function\_Based\_Time\_Aware\_Recommender\_System.pdf](https://thesai.org/Downloads/Volume13No10/Paper_71-Exponential_Decay_Function_Based_Time_Aware_Recommender_System.pdf)  
53. Exponential decay \- Wikipedia, 访问时间为 三月 10, 2026， [https://en.wikipedia.org/wiki/Exponential\_decay](https://en.wikipedia.org/wiki/Exponential_decay)  
54. Counters in Recommendations: From Exponential Decay to Position Debiasing | by Michael Roizner, 访问时间为 三月 10, 2026， [https://roizner.medium.com/counters-in-recommendations-from-exponential-decay-to-position-debiasing-30a6175bba5](https://roizner.medium.com/counters-in-recommendations-from-exponential-decay-to-position-debiasing-30a6175bba5)  
55. Real Time Recommendations with Clickstream Data \- POLITesi, 访问时间为 三月 10, 2026， [https://www.politesi.polimi.it/retrieve/af306c42-370e-408d-90bb-09f077a741ba/Real\_Time\_Recommendations\_With\_Clickstream\_Data.pdf](https://www.politesi.polimi.it/retrieve/af306c42-370e-408d-90bb-09f077a741ba/Real_Time_Recommendations_With_Clickstream_Data.pdf)

[image1]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADkAAAAZCAYAAACLtIazAAACpUlEQVR4Xu2XTahNURTHl6TId0QmvCQlA0kGykQhBj6SgQGmmMpHkbpCykBCIQMZkolk8ibvlomQmTIxUCKJoijk4/9r79XZb73bfZPzcq7Or/6dfdY6+5yzzl577X3MWlpaWv4R08P5XGly1szg68eUaGgSf4K+SKukjdLvHn7nXbBzfaNZb+lFj0eHeG3JNzU6xBppWTQ2FV6WQG5Hh3hjyTcj2EnPO8HWaBZZCuRhsDNKP7KPa0qGpV3B1mg8yG5hmyRdkkayLwZ5X5oWbI2GVCQQ5p9DEActpTA+UhoI/kw+DhQUlRgk6Ujw57KP4gQrpWd+0aBBICwfME/alNtUXHzb8vmtbBtICIR1kWJTjhTB4SMwRrbOYrNCehpsh6UDwVYbBIIoNsw5pwxyr9VbbAjmY7DNtwncPXmQFJsSCs7X7LsWfJHZ0nLrXZSGpJ2W7kcQC6WXltLf12B8PvdLhiz19eBnSVtzGxsDwf3GxYNkPpaQUnztt9Z/d3NMeixdlJ7b6IcS+E1pt3RFWi1dt/Q8Ctx2Sy97ysamb9mX+y6RbkinpX3SA+mk9Nk79IPRuhuNll72lbQgOgoYgU5u+/VO11LwQKofyW1Gm4D4IYBD0jrpWz6HrvQknCM+PLVhQ7b7QIzLWWlpNFpaXo5GY+CyVWnG8Xvho5ixDEWYe/Qr6VjaRjqx7wurNiz05R6wx0Y/s3aYT12rftnuSY+kC9JiSxniGwmHUaTo8LcDO/KRLSR/M518XvalzwdL63QcOT5MRzpvY6dbLfDwq1YVj1+WdknMObLgvVVpxbX7LaUtI0GqMscoWED6lmv0J2lLbm+2avni+DO3gRRfK50obBMCc9YDnVM6LAXLy8eqSx9+zh369+ob/4CijaLlqdvyX/IX+1CLZEFqfQYAAAAASUVORK5CYII=>

[image2]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAsAAAAaCAYAAABhJqYYAAAAuElEQVR4XmNgGAUjEEwH4pdA/AuIXZDEGYH4HxB7IIkx7AZifiD+D8RLoYpAQASIfwOxDZTP4AnEHAwIxdEwCSBIB+KtUHkwgDEsgfgJECtC+SDTQbZUQflwwAPEBxhQTTUG4q9ArIQkBgaaQPwWSsMASCPIWSCDWBkQ/oCbAvIQDMxhgCgGAT8GJHeLAfEVINaB8oWA+A8DRLECA8SJKMCJARLOjxggTjIH4tdA/B6IC5DUjQIwAABw/h7c9DCjbgAAAABJRU5ErkJggg==>

[image3]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACMAAAAZCAYAAAC7OJeSAAACAElEQVR4Xu2VPWgUURDHJ2hAUBBRDBpDQEGw9gMEQYsgpNBGgoVVOiEWgSCWVha2gtgoYp1YCgoWQUsbC8VChBSKlVolkEg0/1/mTZx7u+EIHFzh/eDP7c6bfTtfb89swIAB/wEfpb+1sV/8lr7Xxn6w27wq9+qFfjAi/ZEu1wuJc9Kl2tjCkHRFOl0vdGOPdES6L72VTkiHpV3J5530K90TNFXcm2xAEOvS83K/39xvR3NIixasvUWx4c1kI7i2F1yTPpgnE+D3I913ZVRakiYrO9yRPpm3MfhizWDGpM/SrcqOH8EHdCLv1YAX8tC+yn6j2CcqO7bFyhbVynscKDb2CRgFurAtLNaZwmNz+7FkYy6wPUg2oBX1HgzwcvkN8CP5beH7kvv6xnxWnlnzBRfMfU+ZJxFZ831in4CgnxZxHSyZjwXD33pyeSHlg4PS1XId7QsI8LV5S2gBv8fL2kvr9L1ufrJyi3jmhfnczEt3rWV+Hpkf1xXzI545K61JX6Xb5hnh+1MaT35w0Twg9pkp1wQQEBjVnpOGpem0tgVlPGrNAQ44qnyLAqrXyKiC9tQtZs5I4pV1tq6nnJG+SSeTjepRzYAZi7lkoKkSyZNYT+HU5VND+6jK7JaHz+FqucaPg3D+33Lv4K/jofReeiJNdS5vwtDmMThkPjcDurIBYiFoThEyVYAAAAAASUVORK5CYII=>

[image4]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA8AAAAYCAYAAAAlBadpAAAAxklEQVR4XmNgGAU0A5+A2BhdkBBgAeI5QPwfiIPQ5AgCWyD+yQDRXI4mRxBcBGJ3BojmhWhyeIEsEIcDsSQDRPNSVGncABQ4j6BsHgaI5tMIadyAE4g3A/EEKB+m+SFcBR4A8mcMmhhI81c0MQzACMSngFiVAeJXGAZp/o2kDgOwAvF8BkjcogOQZhDGCVyB+C66IBTg1cwMxNeBWBxdAgpA/sXQDPJjKBDvAuL3QJyCKs0gAMRmQPyPAaLZD4hlUFSMAjoAAAf0JODqVQOJAAAAAElFTkSuQmCC>

[image5]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACEAAAAZCAYAAAC/zUevAAABjElEQVR4Xu2UPyiGQRzHv0IpJBH5U/InJYPBpMQischgsRgsFmYxvSmDGCyyWKVklVUMZLcZKGXCxKLw/fa7457rLe90qOdTn97n+f3uuffufncH5OTk/COu6UccTM0bfYiDKamArcJ6nEhJM32n43EiBVW0hW7Qc9pNm2h52CgFKsURfrkUbfSWTkbxpCzDNmVNnEiJSvEn7ofH4P2M1sE257D7LaPTtC9o5/G5kSA2GL3Xwtr10IEg/oVWQSdDNNAp97xE12iB7tA5egPrzNNLL+gM3aQdtJKuwibjeYb9uW7lohfiLuyOeIUdVc8sbXcx/bGO8zGtdvlTegUbuNCeUmyRDsH6E/V0H9aH+jtx8QxKtqL4xhxzik56H+Q08PBYN8IGoX4K+G6rEi4Ez1vuuSTimR/QSzpPR+kLrPZCE9mm/e79DlYSodOnC1FoAJpMyWjpwpn7jvdgG/eJTricrvuw1qq9Vkn745B2ufgKsnvqR3QqVE+PPvb19/hrvxgagC+xvou/zcnwCX6aPzKr0RmtAAAAAElFTkSuQmCC>

[image6]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAFUAAAAZCAYAAABAb2JNAAAEHElEQVR4Xu2YS4gUZxDHSzQQiSEGReMjGCQXIagoBuLrpGIOelAwgrnkZM5KFETwIJ4FCQREWDz4xJsaETws5hBRL4GooBFUQiSKCoKCBjX1s6ac6rK7Z3Z2ZsVlfvBnd+r7uvvr6q8e3SJ9+vTp06fPe8ZE1bSkDwszqjmn2p+No5CvVeuzsYo9qlc1eqTa+WZ2kQ9UF1WT8sAo5V8x57bNUzEnZr4Sc+wB1bhgn6L6UzU+2HrFZNUV1bd5YIRZqXqRjXXg0JfZKJYKbqtuqqYG+0Ypn98Llqr+U83OAyPMBNWgFP1QyadiTj2dB5QVYs67oPok2J+oToTfvYTrlEXRu4AHfESKUVvKPLFF78gDys9iYz8me9V8+Ei1UDU2D3QIUVIXFRRVcl07xZXdtkpsjZ3ALs1RWwr58m/VzGSfK+a8o8nOgsjBi5Id/lAtFguT6IihhjAO8k6ENfzW+J9c7rDeS2LRBN+JXZOOJDttvuqO2JrHqLaJnfdQnNQmHLcmGyOEPgu7rxoQa4/QLbGDN8jbO47C8Vhsh0dY7O7G37tSdOpmsfPlm20FYcZxdCkZnBfTgt/LvmCDWarrYoXG4YFwLOsaKhy3JRsjOAYHXZamQxEX+6I5rYAXL/5GcDZhD1z4WhjrNC/OEDuurPLna3horgs2IBKZGzsVIobIIYIcjqdNbAXn2p6NEXYnk+bkgRqqnOpwrrxgfj8Iv9uFxQ+K5cLIJrFI8NAHj4Y8FxuFNcLO50F7wfk4/a6jpVMJFyZx0nZp5VRumL6SnetwDfLiUOAGudGy0C+rA+THHA3kZmzcp+OtUXQMG6HWUYGWTmVCXkgrWBRP3kM9UrZgnMs1cDZ2HALkXvJc1QuEFzePotWq843/DzYU8WjwhwFlOXltw0ZqccjD/pvu4MswluHYykLFTTGhk7CsOjGFiAISnYozvLD9KnZTgEM5D31fGZw/RhEO9WM5f3QqPbTvSPJl3JnY/UECRQubh7oXOHY1XcIu1e+NsQz3V7qhvE3Jqq1oibrmnwuT7+6pboi1MbzePZRiESF0mYe9DIrGL6p/xFq4XET+Uj0Xe2A/ic3lfFfFKr7zmeqY2D2eEetM2NWO5+etYtf4QbUkjEeIngGxDdl1eMMip1XBl68F0mzHSAtlOZhdeCobAyyevjcXH+Dc9K3e9DOXjzvZ+RmcS5fgEPo89LOq5cFeBhHi0dJ1uvVB5XvV4WzsEqQEdiY7z6Ftwql8u3BIfziLkCYCeYB7w7hDuoo7vCfgkNhQdwL98bJs7BL0tjjQ8zs7mB6cvB53/jOxsMapfN77RuyNK3NSmoWyp1CUOv1ITbi2CtXhQhVnjbyikjM/Lw6/JjqYlFK2piF9pB4uhNjxbBxlTBf7GN+T4tSngv8BaQXs7a0GqkYAAAAASUVORK5CYII=>

[image7]: <data:image/png;base64,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>

[image8]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAD0AAAAZCAYAAACCXybJAAAC3ElEQVR4Xu2XTahNURTHl1DkOyITHknJQMJATBRi4CMZUEx9TBWK1BVSBpIUMhAzMpEYoNwyETITJQMlkiiKQj7+P2tvZ9/1nlfqXb1zu7/6d89Za+979tp77XX2MevSpUuXDmFUuJ8gDU0aE3z9MTwaBjM/gz5K86Tl0o8+/JnXwU77WrHUfOB7o0O8MPeNiA6xQJoVjXWBwRPYhegQL819o4OddL4UbLViqnlg14OdVfyafLQpuSltCLZakYNuFrYh0gnpTvLFoK9KI4OtVpC6BMb+zRDUDvOUx8cWACbjUPqtNRSpGDTpy2QcST6KHcyVHuZGdYfAeF3BRGlFuqai41uT7s8nW0dAYLyXKV7lShIsPgJl5QeyeM2RHgTbLml7sLUNAkMUL/Zspgx6iw1s8SK4d8E2yf7j6S4HTfEqoYB9Sr7TwRcZJ822votcj7Te/HhLUFOkp+bbJZ8BeFauHSU95n3zZIyVVqdrbCwM/1fCGG6Zb9W/koOOjUhBVuOV9X/62iPdk45Lj6x1EEzEOWmjdFGaL50xfx4Fc6354A9Y73Qv+/K/06Wz0kFpq3RN2i99yB0SFOcb1vtQ1QKreTkazQf/XJocHQWsUCNd5/aZpvlkAFtjSbpmJQiQDxzYKS2WPqd7aEr3wz1iIagty5I9L8w/c1iaGY3mM7Y7GgMnrUpLfr8UPoojr70Ie5d+JQ3zY28m9n1s1QGKvvwHbLLWZ26Tnplvt7ZA+jSt+kS9It2VjknTzDMoH2yAdqwyRYyvOViXfjny8rXWSPdlX/q8NT8nxJVlohrSUfOMnGE+EbRrCwzmlFV757v5KY49S5a8sSoNaYudNGelSG32aF4R0r08I7yXVqXrlVa9Lvn9lq6BLbFI2pfueS7fEcP+tGgTzHAOfHzpMB8EwcSqTh+qeYb+ffWNxSjaKII51YHX6xNps7SwsHc0t82zD7VtXw82yKj46v3NL3oik4Cu8bYrAAAAAElFTkSuQmCC>

[image9]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAcAAAAZCAYAAAD9jjQ4AAAAj0lEQVR4XmNgGOogGYiV0QVBQByI/wNxA5o4HNgAMTO6IE7ACMRqQOyLLgECu4GYnwEiuRBZAuQIHSi7HIiXMkBMAgNPIOZggOg8AcQRMAlk0MoA8QZcFwyIAPFVIP6HLgEC8xkgukC6lRggxsPBXSD+zQAJgBwgnoIsWcAAMfI1EHsjS8AAyEtW6IKDCgAAeqsT6v50szAAAAAASUVORK5CYII=>

[image10]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEgAAAAZCAYAAACSP2gVAAACn0lEQVR4Xu2XT4hPURTHj5iiofGnRhMi2UiahewsUWpsJjt2ClsKGztsrKQpxUKWWM1CSlNmOUVqVpQUEkVSikT+nM/vvjudd96f3+9ZvN9L91PfZt45977fu9/fuefdn0gikUgkOs+ou16nWp5pjcvVMeID/wt/nL6oJlX7Vb9L8pH3Ls74tlmr2qZaZmK7VSfMdR0UwTXVDdUWl8uxT8Iiz/uE8lpCbqVPKHtUO3ywRXZK8Qs8J4NV85zqs7lmHgVhzV6ChXLz2z6hvJWQW+3iPMQdF2ubCdUH1Q/VC9WlfLoW1jRjrjeqnql2mdgSfBAT7rs41cGHk2OM5aFq2sXahmfiy21KLIhDLk6B4EFht0SD5k2MUruqepTlvEGzqlUu1jb/atBh1Vcpzr2seimhmnKwfTCBfhPBgFMSXCUXb4ZxF7O/TWAxTTTI/Rm3qHqiOqK6qbqnGrODSqDXlhlUFe+VlDeILYRxuEqORg7sUR6oKW8aakOYVgsG8QaKTRlTeVZidVQZURXvwY15xQMPdyD7n0nkKEu4lcW6AFt8vYv9lPA2qqPKiKp4D0zgxjRmWyEYQ47JVNSwG3M/4rHEv3UtZ6TcCNb4ScLxoQA3RTRmekzEGnRMht+YI/SZB6rTku9XgxjEmr6p9ro4h8bSJg3RIBqzBZdxm9x1l2uCb8L91K9Jx+fiLLPCxFk4z2phV9jDI+Yy5qiJ8ZOLw2Nl+4gG+eZIuVF272S4p2YPi1yQopGs4aO5ppf+kvDlWiMZZw+Km1SvpOKgCHwbd31QQrlRduM+0QEw57vqueqxhEVPZfHIVtVTCe3BslnCHN54JyX03wu5EQ6O6dt9UMIR4KwPdggq/riE/nHQ5frBj9UrEkzyOyeRSCQSXeYvYrChVsh3aMIAAAAASUVORK5CYII=>

[image11]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA0AAAAZCAYAAADqrKTxAAAAzElEQVR4XmNgGAUDDK4C8X90QULgNxA/RxfEB1gYILa0okvgA+JA/A+I3dAlsAEOIJYE4k4gPgzEykAsBsTMyIqwAZDT1jCQ6DRpIH4AxJ5o4nhBOQMkEHjQJaCgCIjT0QVBTsMXPyJAzIouCIqft0j8Q0DMD2UbA7ENkhwcgGwBhRwICAOxH5QNMr0WiE9D+ShgOgMkjr4xQIIeBjKB2BIqjgEYgViKAXtANADxE3RBQuAXELswQDQTDUD+AfnTFV0CHwA5WQBdkD4AAINBHqfqZIVOAAAAAElFTkSuQmCC>

[image12]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEUAAAAZCAYAAABnweOlAAACTUlEQVR4Xu2XTahNURTHl1AKvUTkK58pGRhgQGRAYiLJgJSBiZGxGD1kIJQUCiUDmZiavyjpvXozmRhQykAYkY/C/9dah/32Pe+98ybHfaf9q393n7XPPveufdbHvmaFQqFQKBT6kJfS79zYMfDvXm6ciJ/S+9zYIRaYb8rxfGI8ZpkvuJRPdAg2Y8R8cxqxRPol7csnOgRpcys31jFHWipdlp5J66TF0sz0pmkMfuDPAem7tNfcX/yeEFLnsXU/dSgPjVNnufTGfDe7yn2bYmc9Y75gXj7xH9kj3WmoJnXiS6iOzdITy1KK1JnSLk5D8I/OU8dsaVFu5HzyMbl+Kg2YF6md8TlDOiRtTO6rWG0+x8OB+7ck16ydH+P1cd0m1fnkVFzTabfHeKWNUzZYQOeBhdLBGJ+WLkiD0k3phPTaxjq1QborHZFGpVXSdemcdC3u4Xmfzd8Gp+a1YW8Lus1XaZv5b79o3lzggblv+D2G2+ZnFBbSmiuOSSvCxsPIOXJvbswPScMxrq4R0cSbOBp2wvZhjHleT6i2wA/pk/Tc/DfAVvOMeGH/NukvOLzM6gstfR3BGuldMsdGpm2cKBiK8aD5/UBqVqHLhrWdPoBvO6z3u3l5BENj8sh4ZL6rJ6Xd5tWc2gF82Qdpk3m4vg07MMbG27ia2PuBV9IN6Yr5IW9SCLM0MnCOWsFxmbAjHPfHHH8PDse4qh1AseVPJnVkl3Q27P3CN/Nmct56o6gWukh6AmRRXpCIprq0A6o8c9W6Rl/aMrXtuFCYnD9CjGJ2t6Yq1wAAAABJRU5ErkJggg==>

[image13]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABcAAAAZCAYAAADaILXQAAABOklEQVR4Xu2TsUoDQRCG/0BSpAohEFG0sDadpFCwFKxtfYA0eQtfQCQgiL1ga5NHCPEBrAUbDcTKIkVM/j9zy5nJriSYSvPBx93t7M7s7c0BG/4VX/TKD66DMp3QgQ/8FiXu0k9YgYP5cJJreucHPWd0CJuo5Je0MDdjkSp9ohc+4PmgR7RI77FcASXVPBVJUoIlVGKhIlr0SvfDpAjhLZMo8QPyxIER8gKeOt2GzdE30r1cQLt894Owgkr+084U05kn0XEokecE+e73XEzoWyiW7BQdxSOsDWOc0jF9plsudgg7El2jKKAEKULva4dtF1On6EiindKgb34wQvhrv5996O9W9qy36udhoAPr7dslDMlrs5XWGS+0mT3rf7jJ7meEBavYoxUtJuewP1ruZmNr5Zju+MENf4gpFCNPLDHLmqwAAAAASUVORK5CYII=>

[image14]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAsAAAAaCAYAAABhJqYYAAAAsUlEQVR4XmNgGAW4wRUg/g/Er9ElsAFhID7FANHAgiaHFegA8XsgTkeXwAY4gHgrFIPYBAHIVJDpIFsIApB7Qe7egS6BC4AUf0UXxAYYgfgvA0QDD5ocCgApDAfiDQwQxZao0ggAUgQycRYDRNN8ID4BxPzIikAAZvUeBoSkBxD/BmIbmCIQgFl9C4jlkcTFGSBOaUUSA5sIwtgAyOR/yAIg3WXIAkhgFwNEnhVdYmQDAB/XIdTVYJT4AAAAAElFTkSuQmCC>

[image15]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEYAAAAaCAYAAAAKYioIAAACfUlEQVR4XuWZvWtUQRTFr6igEBFRlFTBIEgUUdAIBrVKkRC0iIoB/wAFxUbQdklplSIhjSAWIkqCAYlYiEltoVWwEjQoghCsLBT8OMc7Q+7evH2ZRc2+yf7gwMydfZt9583cuW8isn7Z7AMFbPGBnLgK/YJu+YESJqArpr8fug3dgDpN/BQ0Y/rZ8VmaM2Ye2m7609Bh6DX03cQ3QOOmnx3vJd2Yo1CP67+F9ogacR8aNOM7oEumnxXRmI3QbtEb2wqdgU6bz5HL0DbT58y5C3WE/j3R6yw0i6Zlh58xzDnXQnsY6g9t3vx8aBdxEfohK03g99vckw3emCVZXi58+nEGrGbMG6jmg+AT1O2DlieiT4PZ27vaSrwx9gmnGMPlNCvL93TEjJFVZ8wm6A70AdrrxlpJqjH8/VOhbRkTXXKRXtMmMTmXQnc5a+hwFWA9wt9DHYBehja33YeiOYNim5yFdoU24a4Ur4/iThShmak73p+Lv/hgJnCmn/TBEvj5Ez7YiGei5sQtLjfmoIM+WMBO6KkPlsFagMYkO1kxjkOPfLCAmuhGkwzX3QNoQerXayMeQ4tNaFQvyw/WCMzUP6EBN7aW+KS5FmrIMei5aObmB9/VjbYpfaK7UVfoc8aUutgO8H2DpjBxRWqixpSWy6LZnQVXqv7XbncIuiBa4fJv/PVb8xD0FTrn4iyOGI8vbFVnROrzhS/9m4ZfMumDAVae30RPu6oOXw2Sq9gUuHx41lEEp+V5qT/8qSr/3Jj1Ao15Edr7oJtSrVOClsFT/3jyT0OqdkrQMj6KnilFmDv9MWZbwh00GsMZwz531raHVfsr6LpoxW7/hdIUvwFy2pZ3pbnMpwAAAABJRU5ErkJggg==>

[image16]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAkAAAAaCAYAAABl03YlAAAAs0lEQVR4XmNgGKkgA4hN0AWRAScQ/wfiA2jiGMCVAaKYhuAUEJ8H4itAHIQmBwbyQBwOxIxAbArEe4CYG1mBJhCfQeJXAfEaIGZBEmNgBWIhKJsDiLcCcRFCGhPYAPFvIJZBl0AGIBNAAQkyESsAuQHkFpAinABm1XN0CWSQzgAxBeRwnOAqEP8DYhd0ieVA7AdlgxRcB2JxhDQE/ATiAgZIdMQxQEIcA3gwQKIgH12C+gAAbJocALqiBDAAAAAASUVORK5CYII=>

[image17]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEMAAAAZCAYAAABq35PiAAADbElEQVR4Xu2YTahNURTHl3xEviMSA09SoihfEUWhDBhgoExeGTBTFGFyDQwMRFIi9UZGDJTIQLpSEkUGGCmkFEkpBr7Xzz6ru86659xznpf3kvurf/fstfc5Z++191p7nyvSpUuXLl3+OyaqbqpGxooKnqkORKNnl+pnTb3N7hlqzqvuR2MBe1VTXXmF6p0rt/FedciVR6guq96o5jj7OtVXVx4qpqluqMbEisAm1XfVlmBfoLogaZxt4OEprjxT9UJ1TTXa2WeonrryULEjUxXDVYtUw4Kd8gfVwmCX+aqeYDsoKSTWBzvlu8E22CxRfZKSWe0HrPp70cgA44MJEZzhQwTWqM4EG5DEWHoktSpYaZslvxKLWKvaLu0JcrekvlWxUbU0Gh0k34/RWAR5oc4LGfwlVa+kpUdyYtaeq6a3mv1mVlZn8btN0jvuqMZbI0mZ3nfykaT4NhjEZ1eOcP/t7Pq0qtGqymGrvxIa/YjGAsjotLWYtMQbc41tg3ucDQdwLx32vJb0DOAZtPHOeCKdZ5T6uZL6dFHy93p2Sg1nWAfoVBU4jERkjJU06MPOBjYLfrVMzmx0yoPtQXbNgLhe3KqWl5mKYDIa2TXbKY4hrIogVCudQQ6hURxQZLWkdv5l3IuD2I08tIsvxgkMFKd4cKa1R1vz1R2d4bGZ92cMTy1nMIsMKO4kkX2SYneZs+FAXuBDBLCRvQ1mvC9T3PYmqI5KyxmPJZ9s6zqD8OD++Hyj0hll54sicFpTNc7ZGLDlmmPOzkstD4Btj/waGyQdkHCywbmGpe5ntynp3ipo06ldZQK1ZV4VIsCu0JSWM9gCbQWw9C2jA89k+Ru3JLX1IWKdwynGSmnfDWzGq6ANjiyj8DmjMmOROKZzKCuDAxizSbiwK+yXlkOWu3Yk1pNZHafdh9m1B2cekfTt80r1TXUu1yJR59BVlpw99LEZjQOBeCSefahMkuoQYzBl2yM7DuERD1sG2zQnx55Y4SgKwwjOYjUOGgzorOq4s/FxRUcaztZf4ocak/JFdT2zXZXOz+e0fErKk+tfgY8kVkCfpBcjTojsEJxIBwKnZMsvrCbLEb3ZNQ4rgj7giNnBPiiw5K9IygNsmfPy1X9M/HPnhKTwWSXpa7WMyj93/lVwCEm7LiRyHNHJWV3gF0N905epWZFvAAAAAElFTkSuQmCC>

[image18]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAFwAAAAZCAYAAAC8ekmHAAAEeUlEQVR4Xu2ZXailYxTH/zJTNGMGM5GPOsdHykeDlBJxYYhEQhHu5mKouVDKlJKR3CgluZI6DRdSLo0LudiGpvGRKFI+CokiTU1Rvob1m/Wu2c9e7/PsfU7HcfZM+1ers9/1fK9nPWs973ukGTNmzJgxY8ayOM7kApOByW6TMzr94yandb+nkStNfszK/5k5k0fkNlw0b5n8Y7LN5AmTP0zukW/A+mG1qeMLkyezcgU53uQWk7VJ/7PJnUlXZaPJXpPzK/r9Jnck/TRxg8mOrFxhXpQ75tlJf7Hc6GuSvscD8g5qPGVyYVZOCSzsFZNzcsEKs8Hk5qyUh5MFk0tyQeZbtQ3+XFZMETvVnvdqca48KhAdmoTBa558clYUcITuMjkhF3ScaXKryelJv87kRnksBMqp10o4eAzjRP1gj8lvSVfCGFdk5TJgfvOdtObK2g6aXJoLSgj0f8uNHsLziWWlgs9NftVwUH6zqwHH6pCG7W+S90fipQ0JDuMxzp9dvXvlGx83I+rR5oPuGT6Sh7iAhb1TPAfkok/k4w1MthZl18hvNHjiUsBjd8kT5Wcmv4+UjsK6OH1NWNzdGjU48oL6mXhObojIxngndcMw1I+2wSny8ttMzpJ7LJ5APZI1fXwtX8hm+Xy4YnH7YLwAjy5DHJvKzSrzvHxTMSrGLQ2+Xd6G8ZcCBgxbfK/xoYwyEutECCmPamhwdvGqohyPeVOeqCITb5JfH+Mm87B8QmUiC0/DWCQbnsnwjMEm4OELJrd39S83OaD+7YOTxK0koP3u4jl4Vx5KuNJSJ0Iac35No6dkMbAWTnXARv9SPGda8zq8Y1dnZQfGp2F5NJgsOry0xn3y8tKjIDytXCj94rG1+M9poJ/vOnla/T6hubAOjELICggnPNdy1WJh0xh33GWiOS8G/jgrO4ilNMSIAcceXeueGffTHB/x4vBmwMgYu+VpxHLqT6K5sA7KCVMB4QTdSYVuqUQIjbXUaM6LXWq9Et8vj4VlNg6DZzgp8xoavGwDP3T6IDwtb0zQGoe4W75sEGIGxXMJYYs+yhOK8aPfUs/njDJUjYN2A41/887jHmGguoHm1E9YQOyuGWKXydsaxsxyMrUkymTQ1cIJtMZ52eSl4pmN4TTUiKRcLpzYy82GnPFGoY8bWuvkBpEDxoUToC+uuT2+kt8YSHoM+qrJN/Kbw/yRWqNcJ69LYqQdfZT341PlC/vJ5C+T99Xf0PfkfYyDceifGE7y/lT9fmLjWixoOJcvTR6Uj0tsLz9X7OvqTbqzTzqZQNlAjROwpfvLHZMdwQu5ZeSFZYjv16vRqfxliS94rXJeiPJ3iBqMg+Fb/XCDYjPGeSZzOU9Dp6CvWn/E9UkGjw2utQ+I7dVwcqzwX328ImfljSMscUoe6p75MDXuhSc+Xk1y2KMacgThZjnf68lVH2al/J0BjyZmE/f5/cxIjVGeleeVY57l/gPiIvXfqAFPvVb+ZvpYKsuwaUv+B8TRzGXyN9TVgNDzuvof12asFv8CN7759ftiHKcAAAAASUVORK5CYII=>

[image19]: <data:image/png;base64,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>

[image20]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAG0AAAAZCAYAAAA7S6CBAAAFMElEQVR4Xu2ZW6htYxTH/0IRkevJ/cgtIg9CJE6iSJQ4Lp3ydjqeKEKhHMkDpSQhqZ0HSckLSpJWFEKdzoNIPJCcEEood+NnzLHXWGPNOdfinL33UvNXo7Xnd59jfN8Y45tbGhgYGBgYGBhYQ/Y0OaxFDsqN1pjjTe41edrk1qZsby3WGtu4yuSsWjgD2u+ohZUzTP6aITfIlbQWsKn+NNlmcrvJyORukzvlm2tRQfnf1cIWrjO5pJRdqzmNfbncQHfUCrnSqNu3Vqwwp5p8b7J/KT/b5CctttH+0LQxKvvI9fpqrZD3b7PFBGE0fitvyusuqhUrzC3yeSu7mTyqxTUam+yd5ncWl5rsVwvlHuXDWljpM9rL8rorasUKc798XmJa5XQtptHYUEsm59WKfwn9fzPZo1Zk+oyGi6Lu0FK+zuRKk0dMNpa6DG0eNDnR5NhSBxfK68+Xx7AAf8+8b2i63+6NVI4yeUyeuGyYrPoHxmce5mvzHGyEI00OlLfdYLJXbtCU05858nrhYJMP5GP0cbLadR2g20+b307CaA+YXN3IEyY/yxOAeoTZ6bRnVwGugOeHl1tIN5r8kp5pgwECsit8NwqCiFXHLLfwgMy4Wd42OSW1gZiftQJK+VbuJVA6J4DM8z6NFc0vfSIj5fkm+Zroe31TjwTUvZ6eWe/H6Rl3R3vma4N32y6vZ41faHpDBmTLfYZdNhpB8ckkZG0vyRWaOUnentgSkLDwsgGT8lIZ4lTwpbxPgCt41mRrKoN3NW24PC79Hjd5ReMsl/V9JR+P+ovlCq87l7iRNxYn7TONx3rf5JumDjfNenOCwfjZqFvKc+Up+caBM01+kB+ANggPWV9TdLlHdgQKoq66gsvkGVBAu6xMjEa/rKgj0t/UsdMyvPRrmhyXNRxu8rzJjxobLlxQZJn0zaDk8BARlyvPyMsjdoTRuFJUyOZYb3Z9YaRYL23a5gnYJHGy6IsrxaW2wVi9GWSX0SBcTT3K15h8JN/Rz8kDZzYaRuZOlU/IJ00d1wee2cX5ZCP3NPWnadJVBuvl/h4jouxQFHfOLmLjVWrfMFrVA+sZyde7pOk1x3VoltGCA0ze0/RGy+yU0eJFePF4uYgh7Jzw39RjuEqcnjAcCU0YbTRuNgUntWvRuI6RfJyq+DZ2ldGQvvvqvEbDJeIa+9a8U0aLLC7uHuvkOx3FZUIxjDGSK722YRExB/Ehx5OA04lbo391nwHjkLUCp592dS64S26IiD01Ewy3Wd1jmx6IZaz3nFJ+nMZuOPQ4iyV5u9jwbfD+betYJibbVCvk2RF1ZHvQZTTaVKNxEjOcurjDkPTQPsdKXoLkhl/6U18/ocWuD+XRluDOmtY3ZQHZKhuNLJS4R/zLUIYEfUYL74DCg5g7lB8nqO80Aq6xz7jESLxT60mc59vjQ5q+yKJIsjHqv5afwlDMW/JgjdKJO1wbPjf53eQFOifYoTEOSQaZYkD/F+V9qGcOYid/cxort8nn4psfa2o7pVwXYs2c9Hx1aNNFfW/WywmPMVgv988A4y1p9uWa/hiuC/oTavpO4n8CV3OC/Gt7DB53IuCiyyli1/HyXbuPNkdrchxgt0emxulGWQR9xu2CsS6Qz9d2+YZZ65kH1nuu2pVKONlaCxP0wWgYt4t5Y+PALqR+MMbV/dr8zZ2xZuIVThkfJwZWEdwmrj0Id3uI3C1uTnUVQk/E4oFVhsQt/i/G35y2m9X/z1va76iFA6sLyVP+gjILTujGWjjwP+dvsfFiqo3QJdkAAAAASUVORK5CYII=>

[image21]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAwAAAAaCAYAAACD+r1hAAAAsklEQVR4XmNgGAWjgCjQDcQ/gHglED8F4lIgZkVRgQQ0gfg9ELtD+SCF/4G4DMr3AGJuKJthF1SSBSYABQ+h4uJAfBxZAiT4D1kACg4wQORygHgVTBCkGyS4BiaABA4wQOQmADEjTFASKlgOE0ACBxggcmLIgjAbgpAFoWAHA0QOA4BC5xYS3wqITwBxGgPEb/xAXIkkz2AKxHeA+AkUnwJiJwZI0M4C4ndAfBGuehQQAABIMyWU5xwOsQAAAABJRU5ErkJggg==>

[image22]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAwAAAAZCAYAAAAFbs/PAAAA5klEQVR4Xu2Svw4BQRCHR0SioRIK3kDpX0FJotEpJPTeQSW5V1BpVAoPoJDoFMQjaERLQoNCgd/cbrE3t7etxpd8yeU3uzObyRH9CZCGa7jT305a8Ai7sA8vsBw4YVCEVZGdtCEScCFDclyYwLHIYvADByL32cOGyOpwSmp6iBmpwotU1xV86CwEjx5aMg/2RO6TgSUZgiZcwqQscHfuKOnADUyZIR+cm4HBiCwTKvBuBpo2fMOCLPBzeCuSA7zJkNnCGqkpZ3iFT3L8dHyBt8T7zsEsjAdOGNj27yRq/5Hkyb7/H/EFLegkF9IifUQAAAAASUVORK5CYII=>

[image23]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA8AAAAZCAYAAADuWXTMAAAAxElEQVR4Xu2SvQ4BQRRGP6HQ+KtEotBqeAlPpBStlxCJ6LyAp6BXS3QiEoUS35cxdnMzs7uJQrMnOY0z495dgJKfadNewFwW9JXjnDb9hRAPuIMzG8gNrtVs8Cg+6cQGcoLrXRtEBy7uaN00oS9Vb9kgxoivXEHy7EFW9Ez75vMRPdItIlO18p5e6JouP27gpul5q9/TBq18pwckF70DuLWjaJomDG0oglbW5YYNRch8k1n4n+FqQxb60/uJaafpQyX/5g26sTSpQZAM+QAAAABJRU5ErkJggg==>