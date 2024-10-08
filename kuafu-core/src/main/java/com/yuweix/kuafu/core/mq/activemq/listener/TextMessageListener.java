package com.yuweix.kuafu.core.mq.activemq.listener;


import com.yuweix.kuafu.core.mq.activemq.message.IReceiver;
import jakarta.jms.Message;
import jakarta.jms.Session;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.jms.listener.SessionAwareMessageListener;


/**
 * 文本类消息队列监听器
 * @author yuwei
 */
public class TextMessageListener implements SessionAwareMessageListener<Message> {
	private static final Logger log = LoggerFactory.getLogger(TextMessageListener.class);
	private IReceiver receiver;

	public void setReceiver(IReceiver receiver) {
		this.receiver = receiver;
	}

	@Override
	public void onMessage(Message message, Session session) {
		if (receiver == null) {
			return;
		}
		try {
			ActiveMQTextMessage msg = (ActiveMQTextMessage) message;
			receiver.receive(msg.getDestination().getPhysicalName(), msg.getText());
		} catch (Exception e) {
			log.error("==>", e);
		}
	}
}

