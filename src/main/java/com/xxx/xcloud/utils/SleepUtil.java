package com.xxx.xcloud.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: SleepUtil
 * @Description: SleepUtil 专门用来做时间间隔的， 单位是毫秒
 * @author huchao
 * @date 2019年10月24日
 *
 */
public class SleepUtil {

    private static Logger logger = LoggerFactory.getLogger(SleepUtil.class); // 日志记录

    private SleepUtil() {}

    public static void sleep(int sleepTime) {
        logger.debug("---------------------sleep " + sleepTime + " ms ------------------------");
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            logger.error("sleep error: " + e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }

    public static void sleepSecond(int sleepSecond) {
        logger.debug("---------------------sleep " + sleepSecond + " s ------------------------");

        sleep(sleepSecond * 1000);
    }

    public static void sleepMinute(int sleepMinute) {
        logger.debug("---------------------sleep " + sleepMinute + " m ------------------------");

        sleep(sleepMinute * 60 * 1000);
    }
}
