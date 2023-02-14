package cn.zko0.utils;

/**
 * @author duanfuqiang
 * @date 2023/2/14 21:23
 * @description 异常终止虚拟机
 */
public class Panic {
    public static void panic(Exception e){
        e.printStackTrace();
        System.exit(-1);
    }
}
