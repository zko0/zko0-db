package cn.zko0.tm;

/**
 * @author duanfuqiang
 * @date 2023/2/14 21:07
 * @description
 */
public interface TransactionManager {


    //开启事务
    long begin();

    //提交事务
    void commit(long xid);

    //回滚一个事务
    void abort(long xid);

    //查询事务是否正在进行
    boolean isActive(long xid);

    //查询事务是否已提交
    boolean isCommitted(long xid);

    //查询事务是否已被取消
    boolean isAborted(long xid);

    //关闭事务
    void close();
}
