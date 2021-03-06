package bluedot.electrochemistry.utils;

import org.apache.shiro.crypto.hash.Md5Hash;

import java.io.File;
import java.util.UUID;

/**
 * @author Senn
 * @create 2022/3/6 16:53
 */
public class EncryptUtil {

    /**
     *  对密码进行MD5加密
     * @param source : 原始密码
     * @return
     */
    public static String md5Decode(String source, String salt) {

        // 散列次数,即md5的加密次数==md5(md5())
        int hashIterations = 2;
//        String salt = "abcd";

        // 构造方法中：
        // 第一个参数：明文，原始密码
        // 第二个参数：盐，通过使用随机数
        // 第三个参数：散列的次数，比如散列两次，相当 于md5(md5(''))
        Md5Hash md5Hash = new Md5Hash(source, salt, hashIterations);
        String password_dm5 = md5Hash.toString();
        return password_dm5;
    }

    /**
     * 对密码进行MD5校验
     * @param source : 原始密码
     * @param password : 密码
     * @param salt : 盐值(从数据库中获取)
     */
    public static boolean md5Encode(String source, String password, String salt) {

        // 散列次数,即md5的加密次数==md5(md5())
        int hashIterations = 2;

        // 构造方法中：
        // 第一个参数：明文，原始密码
        // 第二个参数：盐，通过使用随机数
        // 第三个参数：散列的次数，比如散列两次，相当 于md5(md5(''))
        Md5Hash md5Hash = new Md5Hash(source, salt, hashIterations);

        String password_dm5 = md5Hash.toString();
        if(!password.equals(password_dm5)) {
            return false;
        }
        return true;
        // 第一个参数：散列算法
        //SimpleHash simpleHash = new SimpleHash("md5", source, salt, hashIterations);
        //System.out.println(simpleHash.toString());
    }

    /**
     * 获取盐值
     * @return String
     */
    public static String getSalt(){
        // 盐值(随机产生)
       return UUID.randomUUID().toString().substring(0, 4);
    }

    /**
     * 对文件内容加密
     * @param o 对象
     * @return 加密后密
     */
    public static String md5EncodeFileData(Object o){
        Md5Hash md5Hash = new Md5Hash(o);
        return md5Hash.toString();
    }
}
