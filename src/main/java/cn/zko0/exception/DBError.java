package cn.zko0.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

/**
 * @author duanfuqiang
 * @date 2023/2/14 21:32
 * @description
 */

@AllArgsConstructor
@Getter
public enum DBError {

    READ_FILE_FALL(new DBException("读取文件失败")),
    BAD_FILE(new DBException("文件损坏")),
    WRITE_FILE_FALL(new DBException("写入文件失败")),

    FILE_DOESNT_EXIST(new DBException("文件不存在")),

    UPDATE_XID_COUNTER_AND_HEADER_FALL(new DBException("更新XID总数和文件Header失败")),

    FILE_ALREADY_EXIST(new DBException("文件已存在")),

    TM_CREATE_FALL(new DBException("TM创建失败")),

    FORCE_FALL(new DBException("强制数据更新磁盘失败")),

    TM_CLOSE_FALL(new DBException("TM关闭失败")),


    FILE_PERMESSION_LAKE(new DBException("缺少文件权限"));

    private DBException dbException;

}
