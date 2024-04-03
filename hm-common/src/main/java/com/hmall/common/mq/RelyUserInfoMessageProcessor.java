package com.hmall.common.mq;

import com.hmall.common.utils.UserContext;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

public class RelyUserInfoMessageProcessor implements MessagePostProcessor {
    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        //将用户信息放入用户头
        message.getMessageProperties().setHeader("user-info", UserContext.getUser());
        return message;
    }
}
