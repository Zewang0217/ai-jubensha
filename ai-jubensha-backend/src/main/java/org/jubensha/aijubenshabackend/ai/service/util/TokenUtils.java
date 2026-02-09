package org.jubensha.aijubenshabackend.ai.service.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Token工具类，用于处理模型的token计数和截断
 * <p>
 * 主要用于BAAI/bge模型系列，这些模型通常有512 token的输入限制
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-09
 * @since 2026
 */
@Slf4j
@Component
public class TokenUtils {

    // BAAI/bge模型的最大token限制
    public static final int BAAI_BGE_MAX_TOKENS = 512;

    // 安全阈值，留有余地
    public static final int SAFE_TOKEN_THRESHOLD = 450;

    // 正则表达式，用于基本的token计数（中文按字符，英文按单词）
    private static final Pattern ENGLISH_WORD_PATTERN = Pattern.compile("\\b[a-zA-Z]+\\b");
    private static final Pattern CHINESE_CHAR_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    /**
     * 估算文本的token数量
     * 基于BAAI/bge模型的token规则
     *
     * @param text 输入文本
     * @return 估算的token数量
     */
    public int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        int tokenCount = 0;

        // 计算中文字符（每个中文字符算1个token）
        Matcher chineseMatcher = CHINESE_CHAR_PATTERN.matcher(text);
        while (chineseMatcher.find()) {
            tokenCount++;
        }

        // 计算英文单词（每个英文单词算1个token）
        Matcher englishMatcher = ENGLISH_WORD_PATTERN.matcher(text);
        while (englishMatcher.find()) {
            tokenCount++;
        }

        // 计算数字和标点符号（简化处理，每5个算1个token）
        String remainingText = text.replaceAll("[\\u4e00-\\u9fa5]", "")
                .replaceAll("\\b[a-zA-Z]+\\b", "")
                .replaceAll("\\s+", "");
        tokenCount += (remainingText.length() + 4) / 5;

        // 计算空格和换行（每10个算1个token）
        Matcher whitespaceMatcher = WHITESPACE_PATTERN.matcher(text);
        int whitespaceCount = 0;
        while (whitespaceMatcher.find()) {
            whitespaceCount++;
        }
        tokenCount += (whitespaceCount + 9) / 10;

        return tokenCount;
    }

    /**
     * 检查文本是否超过token限制
     *
     * @param text 输入文本
     * @return 是否超过限制
     */
    public boolean exceedsTokenLimit(String text) {
        return estimateTokens(text) > BAAI_BGE_MAX_TOKENS;
    }

    /**
     * 检查文本是否超过安全阈值
     *
     * @param text 输入文本
     * @return 是否超过安全阈值
     */
    public boolean exceedsSafeThreshold(String text) {
        return estimateTokens(text) > SAFE_TOKEN_THRESHOLD;
    }

    /**
     * 智能截断文本，确保不超过token限制
     * 优先保留文本开头的重要内容
     *
     * @param text 输入文本
     * @param maxTokens 最大token数
     * @return 截断后的文本
     */
    public String truncateText(String text, int maxTokens) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        int currentTokens = estimateTokens(text);
        if (currentTokens <= maxTokens) {
            return text;
        }

        // 按比例截断文本
        double ratio = (double) maxTokens / currentTokens;
        int targetLength = (int) (text.length() * ratio);

        // 确保至少保留一些内容
        targetLength = Math.max(targetLength, 100);

        // 尝试在句子边界截断
        String truncated = text.substring(0, targetLength);
        int lastPeriod = truncated.lastIndexOf('.');
        int lastComma = truncated.lastIndexOf(',');
        int lastSpace = truncated.lastIndexOf(' ');

        if (lastPeriod > targetLength * 0.8) {
            truncated = truncated.substring(0, lastPeriod + 1);
        } else if (lastComma > targetLength * 0.8) {
            truncated = truncated.substring(0, lastComma + 1);
        } else if (lastSpace > targetLength * 0.8) {
            truncated = truncated.substring(0, lastSpace);
        }

        // 添加省略号
        return truncated + "...";
    }

    /**
     * 使用默认的安全阈值截断文本
     *
     * @param text 输入文本
     * @return 截断后的文本
     */
    public String truncateText(String text) {
        return truncateText(text, SAFE_TOKEN_THRESHOLD);
    }

    /**
     * 分块处理文本
     * 将超长文本分割成多个块，每个块都不超过token限制
     *
     * @param text 输入文本
     * @param chunkSize 每个块的最大token数
     * @return 文本块列表
     */
    public String[] chunkText(String text, int chunkSize) {
        if (text == null || text.isEmpty()) {
            return new String[0];
        }

        int totalTokens = estimateTokens(text);
        if (totalTokens <= chunkSize) {
            return new String[]{text};
        }

        // 估算需要的块数
        int chunkCount = (totalTokens + chunkSize - 1) / chunkSize;
        int approximateChunkLength = text.length() / chunkCount;

        // 实际分块
        StringBuilder currentChunk = new StringBuilder();
        StringBuilder resultBuilder = new StringBuilder();
        int currentTokenCount = 0;

        // 按段落分块
        String[] paragraphs = text.split("\\n\\s*\\n");
        for (int i = 0; i < paragraphs.length; i++) {
            String paragraph = paragraphs[i];
            int paragraphTokens = estimateTokens(paragraph);

            if (currentTokenCount + paragraphTokens <= chunkSize) {
                // 当前块可以容纳这个段落
                currentChunk.append(paragraph);
                if (i < paragraphs.length - 1) {
                    currentChunk.append("\n\n");
                }
                currentTokenCount += paragraphTokens;
            } else {
                // 当前块已满，开始新块
                if (currentChunk.length() > 0) {
                    resultBuilder.append(currentChunk.toString()).append("\n---CHUNK_BOUNDARY---\n");
                }
                currentChunk = new StringBuilder(paragraph);
                if (i < paragraphs.length - 1) {
                    currentChunk.append("\n\n");
                }
                currentTokenCount = paragraphTokens;
            }
        }

        // 添加最后一个块
        if (currentChunk.length() > 0) {
            resultBuilder.append(currentChunk.toString());
        }

        return resultBuilder.toString().split("\\n---CHUNK_BOUNDARY---\\n");
    }

    /**
     * 使用默认的块大小分块处理文本
     *
     * @param text 输入文本
     * @return 文本块列表
     */
    public String[] chunkText(String text) {
        return chunkText(text, SAFE_TOKEN_THRESHOLD);
    }

    /**
     * 为分块添加元数据
     *
     * @param chunks 文本块列表
     * @param originalText 原始文本
     * @return 添加了元数据的文本块列表
     */
    public String[] addChunkMetadata(String[] chunks, String originalText) {
        if (chunks == null || chunks.length == 0) {
            return chunks;
        }

        String[] result = new String[chunks.length];
        for (int i = 0; i < chunks.length; i++) {
            StringBuilder metadata = new StringBuilder();
            metadata.append("[块信息] 总块数: ").append(chunks.length)
                    .append(", 当前块: ").append(i + 1)
                    .append(", 原始文本长度: ").append(originalText.length())
                    .append("\n\n");
            result[i] = metadata.toString() + chunks[i];
        }
        return result;
    }
}
