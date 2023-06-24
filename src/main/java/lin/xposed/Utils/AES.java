package lin.xposed.Utils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * AES 对称加密算法，加解密工具类
 */
public class AES {


    /**
     * AES 的 密钥长度，32 字节，范围：16 - 32 字节
     */
    public static final int SECRET_KEY_LENGTH = 32;
    /**
     * 加密算法
     */
    private static final String KEY_ALGORITHM = "AES";
    /**
     * 字符编码
     */
    private static final Charset CHARSET_UTF8 = StandardCharsets.UTF_8;

    /**
     * 秘钥长度不足 16 个字节时，默认填充位数
     */
    private static final String DEFAULT_VALUE = "0";
    /**
     * 加解密算法/工作模式/填充方式
     */
    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

    /**
     * AES 加密
     *
     * @param data 待加密内容
     * @param key
     * @return 返回Base64转码后的加密数据
     */
    public static String encrypt(String data, String key) {
        try {
            //创建密码器
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            //初始化为加密密码器
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(key));
            byte[] encryptByte = cipher.doFinal(data.getBytes(CHARSET_UTF8));
            // 将加密以后的数据进行 Base64 编码
            return base64Encode(encryptByte);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    /**
     * AES 解密
     *
     * @param base64Data 加密的密文 Base64 字符串
     * @param secretKey  密码
     */
    public static String decrypt(String base64Data, String secretKey) {
        try {
            byte[] data = base64Decode(base64Data);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            //设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(secretKey));
            //执行解密操作
            byte[] result = cipher.doFinal(data);
            return new String(result, CHARSET_UTF8);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    /**
     * 使用密码获取 AES 秘钥
     */
    public static SecretKeySpec getSecretKey(String secretKey) {
        secretKey = toMakeKey(secretKey, SECRET_KEY_LENGTH, DEFAULT_VALUE);
        return new SecretKeySpec(secretKey.getBytes(CHARSET_UTF8), KEY_ALGORITHM);
    }

    /**
     * 如果 AES 的密钥小于 {@code length} 的长度，就对秘钥进行补位，保证秘钥安全。
     *
     * @param secretKey 密钥 key
     * @param length    密钥应有的长度
     * @param text      默认补的文本
     * @return 密钥
     */
    private static String toMakeKey(String secretKey, int length, String text) {
        // 获取密钥长度
        int strLen = secretKey.length();
        // 判断长度是否小于应有的长度
        if (strLen < length) {
            // 补全位数
            StringBuilder builder = new StringBuilder();
            // 将key添加至builder中
            builder.append(secretKey);
            // 遍历添加默认文本
            for (int i = 0; i < length - strLen; i++) {
                builder.append(text);
            }
            // 赋值
            secretKey = builder.toString();
        }
        return secretKey;
    }

    /**
     * 将 Base64 字符串 解码成 字节数组
     */
    public static byte[] base64Decode(String data) {
        return Base64Utils.decode(data);
    }

    /**
     * 将 字节数组 转换成 Base64 编码
     */
    public static String base64Encode(byte[] data) {
        return Base64Utils.encode(data);
    }

    /**
     * 处理异常
     */
    private static void handleException(Exception e) {
        e.printStackTrace();
        LogUtils.addRunLog(e);
    }

    /**
     * 对文件进行AES加密
     *
     * @param sourceFile 待加密文件
     * @param dir        加密后的文件存储路径
     * @param toFileName 加密后的文件名称
     * @param secretKey  密钥
     * @return 加密后的文件
     */
    public static File encryptFile(File sourceFile, String dir, String toFileName, String secretKey) {
        try {
            // 创建加密后的文件
            File encryptFile = new File(dir, toFileName);
            // 根据文件创建输出流
            FileOutputStream outputStream = new FileOutputStream(encryptFile);
            // 初始化 Cipher
            Cipher cipher = initFileAESCipher(secretKey, Cipher.ENCRYPT_MODE);
            // 以加密流写入文件
            CipherInputStream cipherInputStream = new CipherInputStream(
                    new FileInputStream(sourceFile), cipher);
            // 创建缓存字节数组
            byte[] buffer = new byte[1024 * 2];
            // 读取
            int len;
            // 读取加密并写入文件
            while ((len = cipherInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
                outputStream.flush();
            }
            // 关闭加密输入流
            cipherInputStream.close();
            closeStream(outputStream);
            return encryptFile;
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    /**
     * AES解密文件
     *
     * @param sourceFile 源加密文件
     * @param dir        解密后的文件存储路径
     * @param toFileName 解密后的文件名称
     * @param secretKey  密钥
     */
    public static File decryptFile(File sourceFile, String dir, String toFileName, String secretKey) {
        try {
            // 创建解密文件
            File decryptFile = new File(dir, toFileName);
            // 初始化Cipher
            Cipher cipher = initFileAESCipher(secretKey, Cipher.DECRYPT_MODE);
            // 根据源文件创建输入流
            FileInputStream inputStream = new FileInputStream(sourceFile);
            // 获取解密输出流
            CipherOutputStream cipherOutputStream = new CipherOutputStream(
                    Files.newOutputStream(decryptFile.toPath()), cipher);
            // 创建缓冲字节数组
            byte[] buffer = new byte[1024 * 2];
            int len;
            // 读取解密并写入
            while ((len = inputStream.read(buffer)) >= 0) {
                cipherOutputStream.write(buffer, 0, len);
                cipherOutputStream.flush();
            }
            // 关闭流
            cipherOutputStream.close();
            closeStream(inputStream);
            return decryptFile;
        } catch (IOException e) {
            handleException(e);
        }
        return null;
    }

    /**
     * 初始化 AES Cipher
     *
     * @param secretKey  密钥
     * @param cipherMode 加密模式
     * @return 密钥
     */
    private static Cipher initFileAESCipher(String secretKey, int cipherMode) {
        try {
            // 创建密钥规格
            SecretKeySpec secretKeySpec = getSecretKey(secretKey);
            // 获取密钥
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            // 初始化
            cipher.init(cipherMode, secretKeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
            return cipher;
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    /**
     * 关闭流
     *
     * @param closeable 实现Closeable接口
     */
    private static void closeStream(Closeable closeable) {
        try {
            if (closeable != null) closeable.close();
        } catch (Exception e) {
            handleException(e);
        }
    }
}
