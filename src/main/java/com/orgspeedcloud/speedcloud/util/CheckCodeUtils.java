package com.orgspeedcloud.speedcloud.util;


import java.util.Random;

/**
 * 用于生成验证码
 * @author Chen
 */
public class CheckCodeUtils {
    /**
     * 生成n位验证码
     * @param length 验证码长度
     * @return 验证码
     */
    public static String createCode(int length){
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(rand.nextInt(10));
        }
        return sb.toString();
    }
}
