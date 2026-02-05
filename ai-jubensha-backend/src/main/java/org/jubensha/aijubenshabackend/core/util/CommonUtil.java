package org.jubensha.aijubenshabackend.core.util;

import java.util.Random;
import java.util.UUID;

/**
 * 通用工具类
 */
public class CommonUtil {

    private static final Random RANDOM = new Random();

    /**
     * 生成UUID
     *
     * @return UUID字符串
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 生成随机数
     *
     * @param min 最小值
     * @param max 最大值
     * @return 随机数
     */
    public static int generateRandom(int min, int max) {
        return RANDOM.nextInt(max - min + 1) + min;
    }

    /**
     * 检查字符串是否为空
     *
     * @param str 字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 检查对象是否为空
     *
     * @param obj 对象
     * @return 是否为空
     */
    public static boolean isNull(Object obj) {
        return obj == null;
    }
}
