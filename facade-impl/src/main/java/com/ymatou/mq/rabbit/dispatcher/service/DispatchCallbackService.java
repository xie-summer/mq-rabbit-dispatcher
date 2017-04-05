package com.ymatou.mq.rabbit.dispatcher.service;

import com.ymatou.mq.infrastructure.model.CallbackConfig;
import com.ymatou.mq.infrastructure.model.Message;
import com.ymatou.mq.infrastructure.service.MessageConfigService;
import com.ymatou.mq.rabbit.dispatcher.support.AdjustableSemaphore;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 消息分发callack service
 * Created by zhangzhihua on 2017/4/1.
 */
@Component
public class DispatchCallbackService {

    private static final Logger logger = LoggerFactory.getLogger(DispatchCallbackService.class);

    @Autowired
    private MessageConfigService messageConfigService;

    private AdjustableSemaphore semaphore;

    /**
     * 回调处理
     * @param message
     */
    public void invoke(Message message){
        //TODO async callback url
        List<CallbackConfig> callbackConfigList = messageConfigService.getCallbackConfigList(message.getAppId(),message.getQueueCode());
        if(CollectionUtils.isEmpty(callbackConfigList)){
            logger.error("appId:{},queueCode:{} not exist subscribler.",message.getAppId(),message.getQueueCode());
            return;
        }

        for(CallbackConfig callbackConfig:callbackConfigList){
            try {
                doInvokeOne(message,callbackConfig,null);
            } catch (InterruptedException e) {
                //TODO 异常处理&返回值
                logger.error("invoke one error.",e);
            }
        }

    }

    /**
     * 调用一个订阅者
     * @param message
     * @param callbackConfig
     */
    void doInvokeOne(Message message,CallbackConfig callbackConfig,Long timeout) throws InterruptedException {
        //TODO
        if (semaphore != null) {
            if (timeout != null) {
                semaphore.tryAcquire(timeout);
            } else {
                semaphore.acquire();
            }
        }

        //TODO 处理异常
        new AsyncHttpInvokeService(message,callbackConfig).send();
    }

    /**
     *
     * @param message
     * @param callbackConfig
     */
    public void onInvokeSuccess(Message message,CallbackConfig callbackConfig){
        //TODO 更新分发明细状态
    }

    /**
     *
     * @param message
     * @param callbackConfig
     */
    public void onInvokeFail(Message message,CallbackConfig callbackConfig){
        //TODO 更新分发明细状态
    }

}