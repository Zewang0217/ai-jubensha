package org.jubensha.aijubenshabackend.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 对话事实提取服务实现
 * 基于规则从对话中提取关键事实
 */
@Slf4j
@Service
public class FactExtractorImpl implements FactExtractor {

    /**
     * 构造函数
     */
    public FactExtractorImpl() {
        // 无参构造函数
    }

    @Override
    public List<Map<String, Object>> extractFacts(String content) {
        List<String> defaultFactTypes = List.of("时间", "地点", "人物", "事件", "线索");
        return extractFacts(content, defaultFactTypes);
    }

    @Override
    public List<Map<String, Object>> extractFacts(String content, List<String> factTypes) {
        if (content == null || content.isEmpty()) {
            return new ArrayList<>();
        }

        log.debug("开始提取事实，内容长度: {}, 提取类型: {}", content.length(), factTypes);

        List<Map<String, Object>> facts = new ArrayList<>();

        // 根据指定的类型提取事实
        for (String factType : factTypes) {
            List<Map<String, Object>> typeFacts = extractFactsByType(content, factType);
            facts.addAll(typeFacts);
        }

        log.debug("提取事实完成，数量: {}", facts.size());
        return facts;
    }

    @Override
    public List<List<Map<String, Object>>> batchExtractFacts(List<String> contents) {
        List<List<Map<String, Object>>> result = new ArrayList<>();

        for (String content : contents) {
            List<Map<String, Object>> facts = extractFacts(content);
            result.add(facts);
        }

        return result;
    }

    @Override
    public int validateFactsQuality(List<Map<String, Object>> facts) {
        if (facts.isEmpty()) {
            return 0;
        }

        // 简单的质量评分逻辑
        int score = 0;
        int totalFacts = facts.size();
        int validFacts = 0;

        for (Map<String, Object> fact : facts) {
            String factContent = (String) fact.get("content");
            if (factContent != null && factContent.length() > 5 && factContent.length() < 100) {
                validFacts++;
            }
        }

        score = (validFacts * 100) / totalFacts;
        log.debug("事实质量评分: {} (有效: {}, 总: {})", score, validFacts, totalFacts);

        return score;
    }

    /**
     * 根据类型提取事实
     */
    private List<Map<String, Object>> extractFactsByType(String content, String factType) {
        List<Map<String, Object>> facts = new ArrayList<>();

        switch (factType) {
            case "时间":
                facts.addAll(extractTimeFacts(content));
                break;
            case "地点":
                facts.addAll(extractLocationFacts(content));
                break;
            case "人物":
                facts.addAll(extractPersonFacts(content));
                break;
            case "事件":
                facts.addAll(extractEventFacts(content));
                break;
            case "线索":
                facts.addAll(extractClueFacts(content));
                break;
            default:
                log.debug("不支持的事实类型: {}", factType);
                break;
        }

        return facts;
    }

    /**
     * 提取时间事实
     */
    private List<Map<String, Object>> extractTimeFacts(String content) {
        List<Map<String, Object>> facts = new ArrayList<>();

        // 常见时间表达式
        String[] timeExpressions = {
            "昨天", "今天", "明天", "前天", "后天",
            "上周", "本周", "下周", "上月", "本月", "下月",
            "早上", "上午", "中午", "下午", "晚上", "凌晨",
            "[0-9]+点", "[0-9]+:[0-9]+", "[0-9]+时", "[0-9]+分",
            "[0-9]+月[0-9]+日", "[0-9]+年[0-9]+月[0-9]+日"
        };

        for (String patternStr : timeExpressions) {
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                String timeFact = matcher.group();
                Map<String, Object> fact = createFact("时间", timeFact);
                facts.add(fact);
            }
        }

        return facts;
    }

    /**
     * 提取地点事实
     */
    private List<Map<String, Object>> extractLocationFacts(String content) {
        List<Map<String, Object>> facts = new ArrayList<>();

        // 常见地点关键词
        String[] locationKeywords = {
            "花园", "客厅", "餐厅", "卧室", "厨房",
            "书房", "阳台", "车库", "地下室", "阁楼",
            "公园", "广场", "商场", "超市", "医院",
            "学校", "公司", "办公室", "会议室", "走廊",
            "电梯", "楼梯", "卫生间", "洗手间", "厕所",
            "酒吧", "咖啡厅", "餐厅", "饭店", "酒店",
            "电影院", "剧院", "体育馆", "健身房", "游泳池"
        };

        for (String keyword : locationKeywords) {
            if (content.contains(keyword)) {
                Map<String, Object> fact = createFact("地点", keyword);
                facts.add(fact);
            }
        }

        // 提取带修饰的地点，如"张三的家"
        Pattern pattern = Pattern.compile("(.+?)的(家|房子|公寓|别墅|办公室|公司)");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String owner = matcher.group(1);
            String placeType = matcher.group(2);
            String locationFact = owner + "的" + placeType;
            Map<String, Object> fact = createFact("地点", locationFact);
            facts.add(fact);
        }

        return facts;
    }

    /**
     * 提取人物事实
     */
    private List<Map<String, Object>> extractPersonFacts(String content) {
        List<Map<String, Object>> facts = new ArrayList<>();

        // 常见人物称呼
        String[] personKeywords = {
            "张三", "李四", "王五", "赵六", "钱七",
            "孙八", "周九", "吴十", "郑一", "王二",
            "先生", "女士", "小姐", "夫人", "先生",
            "医生", "护士", "老师", "学生", "经理",
            "员工", "老板", "客户", "朋友", "家人"
        };

        for (String keyword : personKeywords) {
            if (content.contains(keyword)) {
                Map<String, Object> fact = createFact("人物", keyword);
                facts.add(fact);
            }
        }

        // 提取带关系的人物，如"张三的朋友"
        Pattern pattern = Pattern.compile("(.+?)的(朋友|家人|同事|同学|邻居|亲戚)");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String person = matcher.group(1);
            String relationship = matcher.group(2);
            String personFact = person + "的" + relationship;
            Map<String, Object> fact = createFact("人物", personFact);
            facts.add(fact);
        }

        return facts;
    }

    /**
     * 提取事件事实
     */
    private List<Map<String, Object>> extractEventFacts(String content) {
        List<Map<String, Object>> facts = new ArrayList<>();

        // 常见事件动词
        String[] eventVerbs = {
            "遇见", "看到", "听到", "发现", "找到",
            "丢失", "得到", "拿走", "放下", "打开",
            "关闭", "进入", "离开", "开始", "结束",
            "讨论", "争吵", "打架", "拥抱", "亲吻",
            "打电话", "发短信", "写信", "见面", "分手"
        };

        for (String verb : eventVerbs) {
            if (content.contains(verb)) {
                // 尝试提取事件的主体和对象
                Pattern pattern = Pattern.compile("(.+?)" + verb + "(.+?)");
                Matcher matcher = pattern.matcher(content);
                if (matcher.find()) {
                    String subject = matcher.group(1).trim();
                    String object = matcher.group(2).trim();
                    // 截取合理长度
                    if (subject.length() > 20) subject = subject.substring(0, 20);
                    if (object.length() > 20) object = object.substring(0, 20);
                    String eventFact = subject + verb + object;
                    Map<String, Object> fact = createFact("事件", eventFact);
                    facts.add(fact);
                } else {
                    // 简单提取包含动词的短语
                    int index = content.indexOf(verb);
                    int start = Math.max(0, index - 20);
                    int end = Math.min(content.length(), index + verb.length() + 20);
                    String eventFact = content.substring(start, end).trim();
                    Map<String, Object> fact = createFact("事件", eventFact);
                    facts.add(fact);
                }
            }
        }

        return facts;
    }

    /**
     * 提取线索事实
     */
    private List<Map<String, Object>> extractClueFacts(String content) {
        List<Map<String, Object>> facts = new ArrayList<>();

        // 常见线索关键词
        String[] clueKeywords = {
            "刀", "枪", "剑", "棍", "棒",
            "血", "血迹", "伤口", "伤痕", "淤青",
            "信", "纸条", "日记", "笔记本", "文件",
            "钥匙", "锁", "门", "窗", "抽屉",
            "手机", "电脑", "平板", "相机", "录音笔",
            "钱", "钱包", "银行卡", "身份证", "护照",
            "药", "毒药", "毒品", "酒瓶", "杯子",
            "衣服", "帽子", "鞋子", "手套", "围巾"
        };

        for (String keyword : clueKeywords) {
            if (content.contains(keyword)) {
                // 尝试提取线索的上下文
                int index = content.indexOf(keyword);
                int start = Math.max(0, index - 30);
                int end = Math.min(content.length(), index + keyword.length() + 30);
                String clueFact = content.substring(start, end).trim();
                Map<String, Object> fact = createFact("线索", clueFact);
                facts.add(fact);
            }
        }

        return facts;
    }

    /**
     * 创建事实对象
     */
    private Map<String, Object> createFact(String type, String content) {
        Map<String, Object> fact = new HashMap<>();
        fact.put("type", type);
        fact.put("content", content);
        fact.put("timestamp", System.currentTimeMillis());
        return fact;
    }
}
