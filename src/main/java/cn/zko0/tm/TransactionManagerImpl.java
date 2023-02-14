package cn.zko0.tm;

import cn.zko0.exception.DBError;
import cn.zko0.exception.DBException;
import cn.zko0.utils.Panic;
import cn.zko0.utils.Parser;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.locks.Lock;

/**
 * @author duanfuqiang
 * @date 2023/2/14 21:12
 * @description
 */
public class TransactionManagerImpl implements TransactionManager{

    //XID文件头长度
    static final int LEN_XID_HEADER_LENGTH=8;

    //每个事务占用的长度
    private static final int XID_FIELD_SIZE=1;

    //事务的三种状态
    private static final byte FIELD_TRAN_ACTIVE=0;
    private static final byte FIELD_TRAN_COMMITTED=1;
    private static final byte FIELD_TRAN_ABOTRED=2;

    //超级事务
    public static final long SUPER_XID=0;

    //XID文件后缀
    static final String XID_SUFFIX=".xid";

    private File file;

    private FileChannel fileChannel;

    private long xidCounter;
    private Lock counterLock;


    private void checkXIDCounter(){
        long fileLen=0;
        try {
            fileLen=file.length();
        } catch (Exception e) {
            //获取温江长度失败
            Panic.panic(new DBException(DBError.READ_FILE_FALL));
        }
        if (fileLen<LEN_XID_HEADER_LENGTH){
            Panic.panic(new DBException(DBError.BAD_FILE));
        }
        //分配8byte
        ByteBuffer buf = ByteBuffer.allocate(LEN_XID_HEADER_LENGTH);
        try {
            fileChannel.position(0  );
            fileChannel.read(buf);
        } catch (IOException e) {
            Panic.panic(e);
        }
        //返回8byte的long数据
        this.xidCounter= Parser.parseLong(buf.array());

    }


    @Override
    public long begin() {
        return 0;
    }

    @Override
    public void commit(long xid) {

    }

    @Override
    public void abort(long xid) {

    }

    @Override
    public boolean isActive(long xid) {
        return false;
    }

    @Override
    public boolean isCommitted(long xid) {
        return false;
    }

    @Override
    public boolean isAborted(long xid) {
        return false;
    }

    @Override
    public void close() {

    }

}
