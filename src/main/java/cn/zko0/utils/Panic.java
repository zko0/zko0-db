package cn.zko0.utils;

import cn.zko0.exception.DBError;
import cn.zko0.exception.DBException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author duanfuqiang
 * @date 2023/2/14 21:23
 * @description 异常终止虚拟机
 */
@Slf4j
public class Panic {
    public static void panic(DBError error){
        log.error(error.getDbException().getMessage());
        DBException e = error.getDbException();
        e.printStackTrace();
        System.exit(-1);
    }
}
