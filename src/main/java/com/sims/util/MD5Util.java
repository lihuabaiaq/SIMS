package com.sims.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    /**
     * 使用MD5算法对字符串进行加密
     *
     * @param input 需要加密的字符串
     * @return 加密后的16进制字符串
     */
    public static String encrypt(String input) {
        try {
            // 创建MessageDigest实例
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 将输入字符串更新到MD5算法中
            md.update(input.getBytes());

            // 执行加密过程
            byte[] digest = md.digest();

            // 将字节数组转换为BigInteger
            BigInteger bigInteger = new BigInteger(1, digest);

            // 将BigInteger转换为16进制字符串
            String hashText = bigInteger.toString(16);

            // 补全长度，确保是32位
            while (hashText.length() < 32) {
                hashText = "0" + hashText;
            }

            return hashText;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }

}


