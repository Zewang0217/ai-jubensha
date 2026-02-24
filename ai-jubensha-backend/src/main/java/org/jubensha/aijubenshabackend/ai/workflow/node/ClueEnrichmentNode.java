package org.jubensha.aijubenshabackend.ai.workflow.node;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.jubensha.aijubenshabackend.ai.service.util.ResponseUtils;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import org.jubensha.aijubenshabackend.models.entity.Clue;
import org.jubensha.aijubenshabackend.models.entity.Scene;
import org.jubensha.aijubenshabackend.models.enums.ClueType;
import org.jubensha.aijubenshabackend.models.enums.ClueVisibility;
import org.jubensha.aijubenshabackend.service.clue.ClueService;
import org.jubensha.aijubenshabackend.service.scene.SceneService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 线索丰富节点
 * 用于在场景加载之前生成迷惑线索，确保每个场景有4-6个线索
 *
 * @author zewang
 * @date 2026-02-22
 */

@Slf4j
public class ClueEnrichmentNode {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 迷惑线索模板
    private static final List<String> DECoy_CLUE_TEMPLATES = new ArrayList<>();

    static {
        // 初始化迷惑线索模板
        DECoy_CLUE_TEMPLATES.add("一张皱巴巴的纸条，上面写着一些无关紧要的购物清单");
        DECoy_CLUE_TEMPLATES.add("一个空的咖啡杯，杯壁上还残留着一些咖啡渍");
        DECoy_CLUE_TEMPLATES.add("一本普通的杂志，随意地放在桌子上");
        DECoy_CLUE_TEMPLATES.add("一串钥匙，看起来是普通的家门钥匙");
        DECoy_CLUE_TEMPLATES.add("一个手机充电器，插在插座上");
        DECoy_CLUE_TEMPLATES.add("一张旧照片，照片上是一些陌生人");
        DECoy_CLUE_TEMPLATES.add("一个钱包，里面只有一些零钱和一张身份证");
        DECoy_CLUE_TEMPLATES.add("一支笔，笔帽已经丢失");
        DECoy_CLUE_TEMPLATES.add("一张便签纸，上面写着一个电话号码");
        DECoy_CLUE_TEMPLATES.add("一个空的矿泉水瓶，放在窗台上");
        DECoy_CLUE_TEMPLATES.add("一本日历，上面标记着一些普通的日期");
        DECoy_CLUE_TEMPLATES.add("一个遥控器，看起来是电视遥控器");
        DECoy_CLUE_TEMPLATES.add("一张电影票根，日期是上个月的");
        DECoy_CLUE_TEMPLATES.add("一个眼镜盒，里面没有眼镜");
        DECoy_CLUE_TEMPLATES.add("一张地图，上面标记着一些无关的地点");
    }

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点：线索丰富");

            // 获取游戏ID和剧本ID
            Long gameId = context.getGameId();
            Long scriptId = context.getScriptId();

            if (scriptId == null) {
                log.error("剧本ID为空，无法丰富线索");
                context.setErrorMessage("剧本ID为空，无法丰富线索");
                return WorkflowContext.saveContext(context);
            }

            try {
                // 获取服务实例
                SceneService sceneService = SpringContextUtil.getBean(SceneService.class);
                ClueService clueService = SpringContextUtil.getBean(ClueService.class);

                // 加载剧本的所有场景
                List<Scene> scenes = sceneService.getScenesByScriptId(scriptId);

                // 如果数据库中没有场景，尝试从JSON解析
                if (scenes == null || scenes.isEmpty()) {
                    String scriptAiOutPut = context.getModelOutput();
                    if (scriptAiOutPut != null && !scriptAiOutPut.isEmpty()) {
                        log.info("数据库中无场景，尝试从JSON解析");
                        JsonNode rootNode = ResponseUtils.extractJson(scriptAiOutPut);
                        scenes = parseScenes(rootNode, scriptId);
                    }
                }

                if (scenes != null && !scenes.isEmpty()) {
                    // 加载剧本的所有线索
                    List<Clue> allClues = clueService.getCluesByScriptId(scriptId);

                    // 构建场景-线索映射
                    Map<String, List<Clue>> sceneClueMap = new HashMap<>();
                    for (Scene scene : scenes) {
                        List<Clue> sceneClues = new ArrayList<>();
                        for (Clue clue : allClues) {
                            if (clue.getScene() != null && clue.getScene().equals(scene.getName())) {
                                sceneClues.add(clue);
                            }
                        }
                        sceneClueMap.put(scene.getName(), sceneClues);
                    }

                    // 为每个场景生成迷惑线索，确保每个场景有4-6个线索
                    List<Clue> generatedClues = new ArrayList<>();
                    Map<String, Integer> sceneClueCountMap = new HashMap<>();
                    
                    for (Scene scene : scenes) {
                        List<Clue> currentClues = sceneClueMap.getOrDefault(scene.getName(), new ArrayList<>());
                        int currentCount = currentClues.size();
                        int targetCount = new Random().nextInt(3) + 4; // 4-6个线索
                        
                        // 记录场景线索数量
                        sceneClueCountMap.put(scene.getName(), currentCount);
                        
                        log.info("场景 {} 当前线索数量: {}, 目标线索数量: {}", scene.getName(), currentCount, targetCount);

                        if (currentCount < targetCount) {
                            int neededClues = targetCount - currentCount;
                            log.info("为场景 {} 生成 {} 个迷惑线索", scene.getName(), neededClues);

                            // 生成迷惑线索
                            for (int i = 0; i < neededClues; i++) {
                                Clue decoyClue = generateDecoyClue(scene, scriptId);
                                generatedClues.add(decoyClue);
                                clueService.createClue(decoyClue);
                            }
                        } else {
                            log.info("场景 {} 线索数量已满足要求（{}个），无需生成迷惑线索", scene.getName(), currentCount);
                        }
                    }

                    // 计算最终线索数量
                    Map<String, Integer> finalSceneClueCountMap = new HashMap<>();
                    List<Clue> updatedAllClues = clueService.getCluesByScriptId(scriptId);
                    for (Scene scene : scenes) {
                        int finalCount = 0;
                        for (Clue clue : updatedAllClues) {
                            if (clue.getScene() != null && clue.getScene().equals(scene.getName())) {
                                finalCount++;
                            }
                        }
                        finalSceneClueCountMap.put(scene.getName(), finalCount);
                        log.info("场景 {} 最终线索数量: {}", scene.getName(), finalCount);
                    }

                    log.info("线索丰富完成，为 {} 个场景生成了 {} 个迷惑线索", scenes.size(), generatedClues.size());
                    
                    // 将线索统计信息保存到WorkflowContext
                    context.setSceneClueCountMap(finalSceneClueCountMap);
                    context.setGeneratedClueCount(generatedClues.size());
                } else {
                    log.warn("没有找到场景，无法丰富线索");
                }

                // 更新WorkflowContext
                context.setCurrentStep("线索丰富");
                context.setSuccess(true);

            } catch (Exception e) {
                log.error("线索丰富失败: {}", e.getMessage(), e);
                context.setErrorMessage("线索丰富失败: " + e.getMessage());
                context.setSuccess(false);
            }

            return WorkflowContext.saveContext(context);
        });
    }

    /**
     * 生成迷惑线索
     *
     * @param scene    场景
     * @param scriptId 剧本ID
     * @return 迷惑线索
     */
    private static Clue generateDecoyClue(Scene scene, Long scriptId) {
        Clue clue = new Clue();
        clue.setScriptId(scriptId);
        clue.setScene(scene.getName());
        clue.setType(ClueType.OTHER);
        clue.setVisibility(ClueVisibility.UNDISCOVERED); // 初始为未探索
        clue.setImportance(1); // 迷惑线索重要度低

        // 随机选择一个迷惑线索模板
        Random random = new Random();
        String template = DECoy_CLUE_TEMPLATES.get(random.nextInt(DECoy_CLUE_TEMPLATES.size()));

        // 生成线索名称和描述
        String clueName = "迷惑线索 " + (random.nextInt(1000) + 1);
        String clueDescription = template;

        clue.setName(clueName);
        clue.setDescription(clueDescription);

        return clue;
    }

    /**
     * 解析场景信息
     * 从JSON节点中提取场景数据并转换为Scene实体对象列表
     *
     * @param rootNode 包含场景信息的JSON根节点
     * @param scriptId 剧本ID，用于关联场景与剧本
     * @return 解析后的场景列表
     */
    private static List<Scene> parseScenes(JsonNode rootNode, Long scriptId) {
        List<Scene> scenes = new ArrayList<>();
        JsonNode scenesNode = rootNode.path("scenes");
        if (scenesNode.isArray()) {
            // 遍历所有场景节点并创建Scene对象
            for (JsonNode sceneNode : scenesNode) {
                Scene scene = new Scene();
                scene.setScriptId(scriptId);
                scene.setName(sceneNode.path("name").asText());
                // 构建格式化的场景描述，包含时间、地点、氛围和详细描述
                scene.setDescription("时间: " + sceneNode.path("time").asText() + "\n" +
                        "地点: " + sceneNode.path("location").asText() + "\n" +
                        "氛围: " + sceneNode.path("atmosphere").asText() + "\n" +
                        "描述: " + sceneNode.path("description").asText());
                scene.setCreateTime(java.time.LocalDateTime.now());
                scenes.add(scene);
            }
        }
        return scenes;
    }
}