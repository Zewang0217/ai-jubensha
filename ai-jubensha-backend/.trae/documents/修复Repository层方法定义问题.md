# 修复Repository层方法定义问题

## 问题分析

通过对比数据库表结构和Repository层的方法定义，我发现了以下问题：

### 1. ScriptRepository 问题
- `findByTitleContaining` 方法：数据库表中没有 `title` 字段，应该是 `name` 字段
- `findByDifficulty` 方法：参数类型应该是 `DifficultyLevel` 枚举，不是 `Integer`
- `findByStatus` 方法：数据库表中没有 `status` 字段
- `findByGenre` 方法：数据库表中没有 `genre` 字段

### 2. ClueRepository 问题
- `findByType` 方法：参数类型应该是枚举类型，不是 `String`

## 修复方案

### 1. 修复 ScriptRepository
- 将 `findByTitleContaining` 改为 `findByNameContaining`
- 将 `findByDifficulty(Integer difficulty)` 改为 `findByDifficulty(DifficultyLevel difficulty)`
- 删除不存在的 `findByStatus` 方法
- 删除不存在的 `findByGenre` 方法
- 添加与数据库表结构匹配的方法

### 2. 修复 ClueRepository
- 将 `findByType(String type)` 改为使用枚举类型参数

## 实施步骤

1. 修改 `ScriptRepository.java` 文件
2. 修改 `ClueRepository.java` 文件
3. 确保所有方法参数类型与实体类字段类型一致
4. 确保所有方法名称与数据库表字段名称匹配