package bluedot.electrochemistry.commons.sender;

import bluedot.electrochemistry.commons.Lifecycle;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * @author Sens
 * @Create 2021/12/16 18:58
 */
public class MailSender extends AbstractSender implements Lifecycle {

    // 创建Properties 类用于记录邮箱的一些属性
    private Properties props = new Properties();

    private String user;

    private String password;

    private Session mailSession;

    public MailSender(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public boolean sendMessage(String emailPath, String content) throws MessagingException {

        // 创建邮件消息
        MimeMessage message = new MimeMessage(mailSession);
        // 设置发件人
        InternetAddress form = new InternetAddress(user);
        message.setFrom(form);

        // 设置收件人的邮箱
        InternetAddress to = new InternetAddress(emailPath);
        message.setRecipient(Message.RecipientType.TO, to);

        // 设置邮件标题
        message.setSubject("[电化学分析系统]");

        // 设置邮件的内容体
        message.setContent(content, "text/html;charset=UTF-8");

        // 最后当然就是发送邮件啦
        Transport.send(message);
        return true;
    }

    @Override
    public void init() {
        // 表示SMTP发送邮件，必须进行身份验证
        props.put("mail.smtp.auth", "true");
        //此处填写SMTP服务器
        props.put("mail.smtp.host", "smtp.163.com");
        //端口号，QQ邮箱端口587
        props.put("mail.smtp.port", "25");
        // 构建授权信息，用于进行SMTP进行身份验证
        Authenticator authenticator = new Authenticator() {

            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        };
        // 使用环境属性和授权信息，创建邮件会话
        mailSession = Session.getInstance(props, authenticator);
    }

    @Override
    public void destroy() {

    }
}
