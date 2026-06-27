/*
 Navicat Premium Dump SQL

 Source Server         : local_mysql
 Source Server Type    : MySQL
 Source Server Version : 80045 (8.0.45)
 Source Host           : localhost:3306
 Source Schema         : prompt_manager

 Target Server Type    : MySQL
 Target Server Version : 80045 (8.0.45)
 File Encoding         : 65001

 Date: 27/06/2026 15:25:37
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for prompt
-- ----------------------------
DROP TABLE IF EXISTS `prompt`;
CREATE TABLE `prompt`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '提示词ID',
  `user_id` bigint NOT NULL COMMENT '所属用户ID',
  `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '提示词标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '提示词正文模板，支持{{变量}}占位符',
  `category` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类，如写作、编程、电商、学习',
  `model_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '适用模型，如GPT、Claude、通义千问',
  `tags` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签，多个标签用逗号分隔',
  `is_favorite` tinyint NOT NULL DEFAULT 0 COMMENT '是否收藏：0否，1是',
  `use_count` int NOT NULL DEFAULT 0 COMMENT '使用次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_status` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常，1删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '提示词表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of prompt
-- ----------------------------
INSERT INTO `prompt` VALUES (1, 1, '公众号文章去AI味改写 Prompt', '请你扮演一名{{角色}}，将下面这段文字改写成{{风格}}风格。要求表达自然、有情绪张力，避免机械化和AI味，保留原文核心观点：{{原文}}', '写作', 'GPT', '公众号,改写,去AI味,爆款文案', 1, 1, '2026-06-24 17:05:05', '2026-06-24 17:19:23', 1);
INSERT INTO `prompt` VALUES (2, 1, '深度文章结构化写作 Prompt', '你是一名具备社会观察、心理分析和内容策划能力的深度写作者。请围绕主题「{{主题}}」生成一篇适合公众号/小红书长文的文章。要求：1）先提炼一个有冲突感的核心观点；2）文章结构包含：日常场景切入、问题本质分析、案例延展、情绪共鸣、方法建议、结尾升华；3）语言自然、有画面感，避免模板化和AI腔；4）观点要克制，不要使用绝对化判断；5）输出完整标题、开头、正文大纲和一段示范正文。补充背景：{{背景信息}}；目标读者：{{目标读者}}；期望风格：{{风格}}。', '写作', 'GPT', '深度写作,公众号,结构化创作,去AI味', 0, 0, '2026-06-24 17:29:43', '2026-06-24 17:29:43', 0);
INSERT INTO `prompt` VALUES (3, 1, '电商主图卖点提炼 Prompt', '你是一名熟悉抖音电商、小红书种草和消费品转化文案的电商视觉策划。请根据产品信息「{{产品信息}}」提炼一套适合电商主图的卖点文案。要求：1）先判断产品的核心消费场景和主要购买人群；2）提炼1个主标题、1个副标题、4个卖点标签；3）文案要有购买吸引力，但不能使用绝对化、医疗化、夸大化表述；4）给出主图画面布局建议，包括产品位置、人物/场景、背景色、信息层级；5）输出适合直接交给设计师的版本。竞品参考特点：{{竞品特点}}；平台：{{平台}}；风格偏好：{{风格偏好}}。', '电商', 'GPT', '电商主图,卖点提炼,抖音商城,视觉策划', 1, 0, '2026-06-24 17:31:21', '2026-06-26 00:12:39', 0);
INSERT INTO `prompt` VALUES (4, 1, '学习笔记转考试复习 Prompt', '你是一名擅长把复杂课程内容转化为考试复习资料的大学助教。请根据以下课程内容「{{课程内容}}」整理一份复习笔记。要求：1）先用一句话说明本部分核心考点；2）按“概念解释—关键步骤—常见题型—易错点—记忆口诀”整理；3）对专业术语进行通俗解释；4）标出可能出现在简答题、选择题、实验报告中的内容；5）最后生成一份5题自测题并附答案。课程名称：{{课程名称}}；考试形式：{{考试形式}}；我的基础：{{学习基础}}。', '学习', 'Claude', '课程复习,知识点整理,考试,大学学习', 0, 0, '2026-06-24 17:31:27', '2026-06-24 17:31:27', 0);
INSERT INTO `prompt` VALUES (5, 1, '课程实验报告优化 Prompt', '你是一名熟悉高校计算机课程实验报告写作规范的指导老师。请根据实验主题「{{实验主题}}」、实验代码/结果「{{实验材料}}」和评分标准「{{评分标准}}」，帮助我优化实验报告。要求：1）先判断当前报告是否覆盖评分点；2）补充实验目的、实验环境、核心原理、实现流程、关键代码说明、实验结果分析、问题与改进；3）语言要像学生认真完成的课程报告，不要像AI生成；4）图表和代码说明要有逻辑衔接；5）输出适合直接粘贴进Word的正文内容。课程名称：{{课程名称}}；报告风格要求：{{格式要求}}。', '学习', 'GPT', '实验报告,课程设计,评分标准,Word写作', 0, 0, '2026-06-24 17:31:33', '2026-06-24 17:31:33', 0);
INSERT INTO `prompt` VALUES (6, 1, '代码报错定位与修复 Prompt', '你是一名耐心的全栈开发助教。请根据我的报错信息「{{报错信息}}」、相关代码「{{相关代码}}」和运行环境「{{运行环境}}」，帮我定位问题。要求：1）先用一句话判断最可能的错误原因；2）按“错误现象—原因分析—修改步骤—验证方法”输出；3）不要一次给太多分支，优先给最可能成功的方案；4）如果涉及配置文件、依赖版本、数据库连接、接口请求，请明确指出应该检查哪里；5）最后给出修改后的代码或配置片段。我的技术水平：{{技术水平}}。', '编程', 'GPT', '代码调试,报错分析,SpringBoot,新手友好', 0, 0, '2026-06-24 17:31:39', '2026-06-24 17:31:39', 0);
INSERT INTO `prompt` VALUES (7, 1, '内容风格拆解与复刻 Prompt', '你是一名擅长审美分析和内容风格迁移的创意策划。请分析参考内容「{{参考内容}}」的风格特征，并帮助我生成同类风格的新内容。要求：1）从画面感、情绪氛围、语言节奏、关键词、叙事结构、适用场景六个维度拆解；2）不要照抄原文，只提炼风格方法；3）输出一份“风格说明书”；4）再根据新主题「{{新主题}}」生成3个可用版本；5）标注每个版本适合的平台和人群。期望氛围：{{期望氛围}}；禁用方向：{{不想要的风格}}。', '创意', 'Claude', '风格分析,内容创作,小红书,审美拆解', 0, 1, '2026-06-24 17:31:46', '2026-06-25 23:57:48', 0);
INSERT INTO `prompt` VALUES (8, 1, '测试-小红书种草文案模板.txt', '你是一名擅长小红书内容策划和消费心理分析的资深文案编辑。\r\n\r\n请根据我提供的产品信息，帮我生成一篇适合小红书发布的种草文案。\r\n\r\n【产品名称】\r\n{{产品名称}}\r\n\r\n【目标人群】\r\n{{目标人群}}\r\n\r\n【核心卖点】\r\n{{核心卖点}}\r\n\r\n【使用场景】\r\n{{使用场景}}\r\n\r\n【文案风格】\r\n{{文案风格}}\r\n\r\n请按照以下要求输出：\r\n\r\n1. 标题要有吸引力，但不要夸张虚假；\r\n2. 开头要有真实生活场景，引发用户共鸣；\r\n3. 正文要自然植入产品卖点，不要像硬广；\r\n4. 语言要轻松、真实、有分享感；\r\n5. 结尾加入温和的行动引导；\r\n6. 额外生成 5 个适合发布时使用的标签。\r\n\r\n请直接输出完整文案。', '创意', '通用', '导入模板,用户自带', 0, 0, '2026-06-25 10:22:36', '2026-06-25 10:25:38', 1);
INSERT INTO `prompt` VALUES (9, 1, '测试-小红书种草文案模板.txt', '你是一名擅长小红书内容策划和消费心理分析的资深文案编辑。\r\n\r\n请根据我提供的产品信息，帮我生成一篇适合小红书发布的种草文案。\r\n\r\n【产品名称】\r\n{{产品名称}}\r\n\r\n【目标人群】\r\n{{目标人群}}\r\n\r\n【核心卖点】\r\n{{核心卖点}}\r\n\r\n【使用场景】\r\n{{使用场景}}\r\n\r\n【文案风格】\r\n{{文案风格}}\r\n\r\n请按照以下要求输出：\r\n\r\n1. 标题要有吸引力，但不要夸张虚假；\r\n2. 开头要有真实生活场景，引发用户共鸣；\r\n3. 正文要自然植入产品卖点，不要像硬广；\r\n4. 语言要轻松、真实、有分享感；\r\n5. 结尾加入温和的行动引导；\r\n6. 额外生成 5 个适合发布时使用的标签。\r\n\r\n请直接输出完整文案。', '创意', '通用', '导入模板,用户自带', 0, 0, '2026-06-25 10:24:38', '2026-06-25 10:25:05', 1);
INSERT INTO `prompt` VALUES (10, 1, '测试-小红书种草文案模板.txt', '你是一名擅长小红书内容策划和消费心理分析的资深文案编辑。\r\n\r\n请根据我提供的产品信息，帮我生成一篇适合小红书发布的种草文案。\r\n\r\n【产品名称】\r\n{{产品名称}}\r\n\r\n【目标人群】\r\n{{目标人群}}\r\n\r\n【核心卖点】\r\n{{核心卖点}}\r\n\r\n【使用场景】\r\n{{使用场景}}\r\n\r\n【文案风格】\r\n{{文案风格}}\r\n\r\n请按照以下要求输出：\r\n\r\n1. 标题要有吸引力，但不要夸张虚假；\r\n2. 开头要有真实生活场景，引发用户共鸣；\r\n3. 正文要自然植入产品卖点，不要像硬广；\r\n4. 语言要轻松、真实、有分享感；\r\n5. 结尾加入温和的行动引导；\r\n6. 额外生成 5 个适合发布时使用的标签。\r\n\r\n请直接输出完整文案。', '创意', '通用', '导入模板,用户自带', 0, 0, '2026-06-25 10:25:16', '2026-06-25 10:25:25', 1);
INSERT INTO `prompt` VALUES (11, 1, '测试-小红书种草文案模板', '你是一名擅长小红书内容策划和消费心理分析的资深文案编辑。\r\n\r\n请根据我提供的产品信息，帮我生成一篇适合小红书发布的种草文案。\r\n\r\n【产品名称】\r\n{{产品名称}}\r\n\r\n【目标人群】\r\n{{目标人群}}\r\n\r\n【核心卖点】\r\n{{核心卖点}}\r\n\r\n【使用场景】\r\n{{使用场景}}\r\n\r\n【文案风格】\r\n{{文案风格}}\r\n\r\n请按照以下要求输出：\r\n\r\n1. 标题要有吸引力，但不要夸张虚假；\r\n2. 开头要有真实生活场景，引发用户共鸣；\r\n3. 正文要自然植入产品卖点，不要像硬广；\r\n4. 语言要轻松、真实、有分享感；\r\n5. 结尾加入温和的行动引导；\r\n6. 额外生成 5 个适合发布时使用的标签。\r\n\r\n请直接输出完整文案。', '创意', '通用', '导入模板,用户自带', 0, 0, '2026-06-25 10:25:32', '2026-06-26 11:44:37', 1);
INSERT INTO `prompt` VALUES (12, 1, '一寸免冠证件照生成', '请将照片生成标准一寸免冠证件照，背景为{{背景颜色}}，人物正面面对镜头，五官清晰自然，表情端正，发型整洁，穿着{{服装颜色}}简洁上衣。保持真实人像质感，光线均匀，面部无夸张美颜，不改变人物身份特征，输出适合证件照使用的清晰照片', '创意', 'GPT', '证件照,一寸照,免冠照,{{背景颜色}},人像处理', 0, 0, '2026-06-26 00:21:40', '2026-06-26 00:21:40', 0);
INSERT INTO `prompt` VALUES (13, 1, '测试-小红书种草文案模板-修改后', '你是一名擅长小红书内容策划和消费心理分析的资深文案编辑。\r\n\r\n请根据我提供的产品信息，帮我生成一篇适合小红书发布的种草文案。\r\n\r\n【产品名称】\r\n{{产品名称}}\r\n\r\n【目标人群】\r\n{{目标人群}}\r\n\r\n【核心卖点】\r\n{{核心卖点}}\r\n\r\n【使用场景】\r\n{{使用场景}}\r\n\r\n【文案风格】\r\n{{文案风格}}\r\n\r\n请按照以下要求输出：\r\n\r\n1. 标题要有吸引力，但不要夸张虚假；\r\n2. 开头要有真实生活场景，引发用户共鸣；\r\n3. 正文要自然植入产品卖点，不要像硬广；\r\n4. 语言要轻松、真实、有分享感；\r\n5. 结尾加入温和的行动引导；\r\n6. 额外生成 5 个适合发布时使用的标签。\r\n\r\n请直接输出完整文案。', '创意', '通用', '导入模板,用户自带', 0, 0, '2026-06-26 11:45:35', '2026-06-26 11:46:59', 0);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `user_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录用户名',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密后的密码',
  `nick_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_name`(`user_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', 'dff1eebf48828a7b838c740a71ff0d84', '管理员', '2026-06-24 16:44:18');

SET FOREIGN_KEY_CHECKS = 1;
