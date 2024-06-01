package com.orgspeedcloud.speedcloud.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;

/**
 * 邮件发送工具类
 * @author Chen
 */
@Component
@Slf4j
public class MailUtils {
    @Value("${spring.mail.username}")
    private String from;
    @Resource
    private JavaMailSender mailSender;

    /**
     * 发送邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     * @param isHtml 是否是HTML
     */
    public void sendEmail(String to,String subject,String content,boolean isHtml){
        try{
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            helper.setFrom(from);
            helper.addTo(to);
            helper.setText(content, isHtml);
            helper.setSubject(subject);
            mailSender.send(mimeMessage);
        }catch (Exception ex){
            log.error("mail error", ex);
            throw new RuntimeException("邮件系统出现问题,请联系管理员紧急修复");
        }
    }
}
