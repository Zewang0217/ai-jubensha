## 修改嵌入式大模型配置计划

### 1. 修改application.yml配置文件
- 添加SiliconFlow的API配置
- 更新嵌入模型相关配置
  - api-key: sk-grybheaiwxaghhrftrzjqcluzphujxmiirpncdlhgntgftmc
  - base-url: https://api.siliconflow.cn/v1
  - embedding-model: BAAI/bge-large-zh-v1.5

### 2. 修改AIConfig.java配置类
- 确保嵌入模型配置正确使用SiliconFlow的API
- 保持使用OpenAiEmbeddingModel.builder()，因为SiliconFlow提供OpenAI兼容的API
- 确保baseUrl和apiKey正确传递给嵌入模型

### 3. 验证EmbeddingService.java
- 确保嵌入服务能够正确使用修改后的嵌入模型
- 不需要修改服务实现，因为它已经通过依赖注入使用配置好的嵌入模型

### 4. 验证修改
- 确保所有配置正确更新
- 确保没有语法错误或配置冲突

### 实施步骤
1. 首先修改application.yml文件，更新AI配置部分
2. 然后检查AIConfig.java文件，确保配置正确应用
3. 最后验证EmbeddingService.java，确保服务能够正确使用修改后的嵌入模型

### 预期结果
- 嵌入式大模型将使用SiliconFlow的API
- 嵌入模型将使用BAAI/bge-large-zh-v1.5模型
- 所有嵌入相关功能将正常工作