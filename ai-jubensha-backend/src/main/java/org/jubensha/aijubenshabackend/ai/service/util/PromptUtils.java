package org.jubensha.aijubenshabackend.ai.service.util;


import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 提示词工具类
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-05 18:00
 * @since 2026
 */
public class PromptUtils {

    /**
     * 创建自定义的user message
     *
     * @param template 消息模板
     * @param params   参数映射
     * @return 格式化后的消息
     */
    public static String createUserMessage(String template, Map<String, Object> params) {
        if (template == null || params == null) {
            return template;
        }

        // 使用正则表达式替换模板中的占位符
        Pattern pattern = Pattern.compile("\\{\\{([^\\}]+)\\}\\}");
        Matcher matcher = pattern.matcher(template);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1).trim();
            Object value = params.get(key);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 创建DM消息模板
     *
     * @param type    消息类型
     * @param content 消息内容
     * @return 消息模板
     */
    public static String createDMTemplate(String type, String content) {
        return "DM消息（" + type + "）：" + content;
    }

    /**
     * 创建玩家消息模板
     *
     * @param playerId 玩家ID
     * @param content  消息内容
     * @return 消息模板
     */
    public static String createPlayerTemplate(String playerId, String content) {
        return "玩家（" + playerId + "）：" + content;
    }

    /**
     * 创建讨论消息模板
     *
     * @param topic   讨论主题
     * @param content 讨论内容
     * @return 消息模板
     */
    public static String createDiscussionTemplate(String topic, String content) {
        return "讨论主题：" + topic + "\n讨论内容：" + content;
    }

    /**
     * 创建单聊消息模板
     *
     * @param senderId   发送者ID
     * @param receiverId 接收者ID
     * @param content    消息内容
     * @return 消息模板
     */
    public static String createPrivateChatTemplate(String senderId, String receiverId, String content) {
        return "单聊消息\n发送者：" + senderId + "\n接收者：" + receiverId + "\n消息内容：" + content;
    }

    /**
     * 创建答题消息模板
     *
     * @param playerId 玩家ID
     * @param answer   答案
     * @return 消息模板
     */
    public static String createAnswerTemplate(String playerId, String answer) {
        return "玩家（" + playerId + "）的答案：" + answer;
    }

    /**
     * 创建评分消息模板
     *
     * @param playerId 玩家ID
     * @param score    评分
     * @param comment  评语
     * @return 消息模板
     */
    public static String createScoreTemplate(String playerId, int score, String comment) {
        return "玩家（" + playerId + "）的评分：" + score + "分\n评语：" + comment;
    }

    /**
     * 创建系统消息模板
     *
     * @param type    消息类型
     * @param content 消息内容
     * @return 消息模板
     */
    public static String createSystemTemplate(String type, String content) {
        return "系统消息（" + type + "）：" + content;
    }
}