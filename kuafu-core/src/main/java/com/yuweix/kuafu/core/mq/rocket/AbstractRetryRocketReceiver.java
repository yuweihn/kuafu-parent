package com.yuweix.kuafu.core.mq.rocket;


import com.yuweix.kuafu.core.SpringContext;
import org.apache.rocketmq.common.message.Message;
import org.springframework.retry.support.RetryTemplate;


/**
 * @author yuwei
 **/
public abstract class AbstractRetryRocketReceiver<T> extends AbstractRocketReceiver<T> {
    private RetryTemplate retryTemplate;
    private final Object LOCK = new Object();


    @Override
    public void onMessage(Message message) {
        getRetryTemplate().execute(retryCallback -> {
            super.handleMessage(message);
            return null;
        }, recoveryCallback -> null);
    }

    @Override
    protected void handleException(Message message, Exception ex) {
        throw new RuntimeException(ex);
    }

    public void setRetryTemplate(RetryTemplate retryTemplate) {
        this.retryTemplate = retryTemplate;
    }

    protected RetryTemplate getRetryTemplate() {
        if (retryTemplate != null) {
            return retryTemplate;
        }
        synchronized (LOCK) {
            if (retryTemplate == null) {
                retryTemplate = SpringContext.getBean(RocketRetryTemplate.class);
            }
        }
        return retryTemplate;
    }
}
