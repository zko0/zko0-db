package cn.zko0.tm;

import cn.zko0.exception.DBError;
import cn.zko0.utils.Panic;
import cn.zko0.utils.Parser;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.locks.Lock;

/**
 * @author duanfuqiang
 * @date 2023/2/14 21:12
 * @description
 */
@Slf4j
public class TransactionManagerImpl implements TransactionManager{

    //XID文件头长度，Header记录了xid事务总数
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

    private RandomAccessFile file;

    private FileChannel fileChannel;

    private long xidCounter;
    private Lock counterLock;

    private TransactionManagerImpl(RandomAccessFile file, FileChannel fileChannel) {
        this.file = file;
        this.fileChannel = fileChannel;
    }

    //创建TM
    public static TransactionManager create(String path){
        File f = new File(path);
        try {
            if (!f.createNewFile()){
                Panic.panic(DBError.FILE_ALREADY_EXIST);
            }
        } catch (IOException e) {
            Panic.panic(DBError.TM_CREATE_FALL);
        }
        if (!f.canRead()||!f.canWrite()){
            Panic.panic(DBError.FILE_PERMESSION_LAKE);
        }
        //filechannel
        RandomAccessFile randomAccessFile=null;
        FileChannel tempFileChannel=null;

        try {
            randomAccessFile=new RandomAccessFile(f,"rw");
            tempFileChannel=randomAccessFile.getChannel();
        } catch (FileNotFoundException e) {
            Panic.panic(DBError.READ_FILE_FALL);
        }
        // 写空XID文件头
        ByteBuffer buf = ByteBuffer.allocate(8);
        try {
            tempFileChannel.position(0);
            tempFileChannel.write(buf);
        } catch (IOException e) {
            Panic.panic(DBError.WRITE_FILE_FALL);
        }
        return new TransactionManagerImpl(randomAccessFile,tempFileChannel);
    }

    public static TransactionManager open(String path){
        File f = new File(path);
        if (!f.exists()){
            Panic.panic(DBError.FILE_DOESNT_EXIST);
        }
        if (!f.canRead()||!f.canWrite()){
            Panic.panic(DBError.FILE_PERMESSION_LAKE);
        }
        //filechannel
        RandomAccessFile randomAccessFile=null;
        FileChannel tempFileChannel=null;

        try {
            randomAccessFile=new RandomAccessFile(f,"rw");
            tempFileChannel=randomAccessFile.getChannel();
        } catch (FileNotFoundException e) {
            Panic.panic(DBError.READ_FILE_FALL);
        }
        return new TransactionManagerImpl(randomAccessFile,tempFileChannel);
    }


    private void checkXIDCounter(){
        long fileLen=0;
        try {
            fileLen=file.length();
        } catch (Exception e) {
            //获取长度失败
            Panic.panic(DBError.READ_FILE_FALL);
        }
        if (fileLen<LEN_XID_HEADER_LENGTH){
            Panic.panic(DBError.BAD_FILE);
        }
        //分配8byte,buf读取为xid的头8字节的long型数据
        ByteBuffer buf = ByteBuffer.allocate(LEN_XID_HEADER_LENGTH);
        try {
            //从头读取文件内容
            fileChannel.position(0  );
            fileChannel.read(buf);
        } catch (IOException e) {
            //读取失败
            Panic.panic(DBError.READ_FILE_FALL);
        }
        //返回8byte的long数据
        this.xidCounter= Parser.parseLong(buf.array());
        long end = getXidPosition(this.xidCounter + 1);
        if (end!=fileLen){
            Panic.panic(DBError.BAD_FILE);
        }
    }


    @Override
    public long begin() {
        //开启事务获取锁
        try {
            counterLock.lock();
            long xid = xidCounter + 1;
            //更新Xid为事务开启状态
            updateXid(xid,FIELD_TRAN_ACTIVE);
            //更新Counter和文件Header
            upodateXIDCounterAndHeader();
            return xid;
        } finally {
            //释放锁
            counterLock.unlock();
        }
    }

    @Override
    public void commit(long xid) {
        //更新Xid为事务提交状态
        updateXid(xid,FIELD_TRAN_COMMITTED);
    }

    @Override
    public void abort(long xid) {
        //更新Xid为事务废弃状态
        updateXid(xid,FIELD_TRAN_ABOTRED);
    }

    @Override
    public boolean isActive(long xid) {
        return checkXID(xid, FIELD_TRAN_ACTIVE);
    }

    @Override
    public boolean isCommitted(long xid) {
        return checkXID(xid, FIELD_TRAN_COMMITTED);
    }

    @Override
    public boolean isAborted(long xid) {
        return checkXID(xid, FIELD_TRAN_ABOTRED);
    }

    @Override
    public void close() {
        try {
            fileChannel.close();
            file.close();
        } catch (IOException e) {
            Panic.panic(DBError.TM_CLOSE_FALL);
        }
    }


    //获取对应xid在文件中的位置
    private long getXidPosition(long xid){
        return LEN_XID_HEADER_LENGTH+(xid-1)*XID_FIELD_SIZE;
    }

    /**
     * @description 更新xid事务状态为status
     * @param xid xid值
     * @param status 事务状态
     */
    private void updateXid(long xid,byte status){
        //获取xid的位置
        long xidPosition = getXidPosition(xid);
        //将事务状态写入文件
        byte[] temparr = new byte[XID_FIELD_SIZE];
        temparr[0]=status;
        ByteBuffer buffer = ByteBuffer.wrap(temparr);
        //使用nio写入文件
        //调整channel位置
        try {
            fileChannel.position(xidPosition);
            fileChannel.write(buffer);
        } catch (IOException e) {
            Panic.panic(DBError.WRITE_FILE_FALL);
        }
        //要求Filechanel强制更新到磁盘
        try {
            fileChannel.force(false);
        } catch (IOException e) {
            Panic.panic(DBError.FORCE_FALL);
        }
    }

    private void upodateXIDCounterAndHeader(){
        xidCounter++;
        ByteBuffer header = ByteBuffer.wrap(Parser.long2Byte(xidCounter));
        try {
            fileChannel.position(0);
            fileChannel.write(header);
        } catch (IOException e) {
            Panic.panic(DBError.UPDATE_XID_COUNTER_AND_HEADER_FALL);
        }
        //要求Filechanel强制更新到磁盘
        try {
            fileChannel.force(false);
        } catch (IOException e) {
            Panic.panic(DBError.FORCE_FALL);
        }
    }

    /**
     *
     * @param xid
     * @param status
     * @return xid检查方法，status传入值比较
     */
    private boolean checkXID(long xid,byte status){
        //获取xid的位置
        long xidPosition = getXidPosition(xid);
        ByteBuffer buffer = ByteBuffer.allocate(1);
        //调整channel位置
        try {
            fileChannel.position(xidPosition);
            fileChannel.read(buffer);
        } catch (IOException e) {
            Panic.panic(DBError.READ_FILE_FALL);
        }
        return xid==buffer.array()[0];
    }

}
