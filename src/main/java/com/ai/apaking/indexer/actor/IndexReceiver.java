package com.ai.apaking.indexer.actor;

import akka.actor.UntypedActor;
import com.ai.apaking.common.commonClass.FFS;
import com.ai.apaking.common.commonClass.Result;
import com.ai.apaking.common.commonClass.ResultCode;
import com.ai.apaking.indexer.Context;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * 类型说明: Index Receiver,it receives message from Forwarding Service.
 *
 * @author guh
 *         create time 2016/3/16 10:00
 */
public class IndexReceiver extends UntypedActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexReceiver.class);


    @Override
    public void preStart() {
        //预留
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof FFS) {
            //single FFS
            receiveFFS((FFS) message);

        } else if (message instanceof List) {
            //FFS list
            receiveFFSList((List) message);
        } else {
            unhandled(message);
        }
    }

    /**
     * check FFS format.if any value of Host/Source/SourceType/Text is Empty returns FFS_FORMAT_ERROR ,
     * else return SUCCESS
     *
     * @param ffs FFS
     * @return Result Code
     */
    private int checkFFS(FFS ffs) {

        if (StringUtils.isEmpty(ffs.getHost()) ||
                StringUtils.isEmpty(ffs.getSource()) ||
                StringUtils.isEmpty(ffs.getSource()) ||
                StringUtils.isEmpty(ffs.getSourceType()) ||
                StringUtils.isEmpty(ffs.getText())) {
            return ResultCode.EXCEPTION_FFS_FORMAT_ERROR;
        } else {
            return ResultCode.SUCCESS;
        }
    }

    /**
     * Deal single FFS
     *
     * @param ffs FFS Object
     */
    private void receiveFFS(FFS ffs) {
        Result result = new Result();

        int resultCode = checkFFS(ffs);


        if (resultCode == ResultCode.SUCCESS) {
            Context.getContext().getBuffer().add(ffs);
            result.setMessage("FFS接收成功!");
            LOGGER.debug("Indexer Accepted FFS:" + ffs.toString());
        } else {
            result.setMessage("FFS格式检查出错,已抛弃!");
            //TODO 告知Audit Service

            LOGGER.debug("Indexer Denied FFS:" + ffs.toString());
        }

        result.setResultCode(resultCode);

        getSender().tell(result, getSelf());
    }


    /**
     * Deal FFS List
     *
     * @param ffsList List of FFS Objects
     */
    private void receiveFFSList(List ffsList) {
        Result result = new Result();
        int resultCode;
        FFS ffs;
        for (Object row : ffsList) {
            if (row instanceof FFS) {
                ffs = (FFS) row;
                resultCode = checkFFS(ffs);
                if (resultCode == ResultCode.SUCCESS) {
                    Context.getContext().getBuffer().add(ffs);
                    LOGGER.debug("Indexer Accepted FFS:" + ffs.toString());
                } else if (result.getResultCode() != ResultCode.EXCEPTION_FFS_FORMAT_ERROR) {
                    //只要有一个格式不通过,结果就是错
                    result.setResultCode(resultCode);
                    //TODO 告知Audit Service
                    LOGGER.debug("Indexer Denied FFS:" + ffs.toString());
                } else {
                    LOGGER.debug("Indexer Denied FFS:" + ffs.toString());
                }
            } else {
                //TODO 告知Audit Service

            }
        }

        if (result.getResultCode() != ResultCode.EXCEPTION_FFS_FORMAT_ERROR) {
            result.setResultCode(ResultCode.SUCCESS);
            result.setMessage("整批FFS接收成功!");
        } else {
            result.setMessage("部分FFS格式检查出错,已抛弃!");
        }

        //send ResultObjcet to forwarding
        getSender().tell(result, getSelf());
    }
}

