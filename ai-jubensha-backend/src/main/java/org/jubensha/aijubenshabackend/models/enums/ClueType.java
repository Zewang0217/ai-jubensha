package org.jubensha.aijubenshabackend.models.enums;

/**
 * 线索类型, 所谓:
 * 文字线索：最常规，纸条、日记、报告、信件等文字形式；
 * 实物线索：实景本居多，凶器、信物、道具等实体物品；
 * 音频 / 视频线索：进阶设计，录音、监控视频、语音留言等；
 */
public enum ClueType {
    DOCUMENT,
    PHYSICAL,
    MEDIA,
    OTHER
}