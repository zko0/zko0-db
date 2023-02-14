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

    READ_FILE_FALL("读取文件失败"),
    BAD_FILE("文件损坏");

    private String message;

}
