package io.transport.common.email;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Email通知消息发送
 * 
 * @author Wangl.sir <448797381@qq.com>
 * @version v1.0
 * @date 2017年9月4日
 * @since
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class MailSenderService implements InitializingBean, DisposableBean {
	final private static Logger logger = LoggerFactory.getLogger(MailSenderService.class);
	final private static ExecutorService executor = Executors.newFixedThreadPool(2);

	@Value("${spring.application.name:defaultSystem}")
	private String systemName = "defaultSystem";
	@Value("${mail.msg.limit:8000}")
	private Integer msglimit = 8000;
	@Value("${mail.from.host:smtp.exmail.qq.com}")
	private String host = "smtp.exmail.qq.com";
	@Value("${mail.from.username:syslog@7782.co}")
	private String sender = "syslog@7782.co";
	@Value("${mail.from.passwd:Ajy@log82}")
	private String passwd = "Ajy@log82";
	@Value("${mail.smtp.auth:true}")
	private Boolean smtpAuth = true;
	@Value("${mail.smtp.timeout:25000}")
	private Integer smtpTimeout = 25000;
	@Value("${mail.admin.list:}")
	private String[] emailAdminlist;
	@Value("${mail.admin.enable:false}")
	private Boolean enable = false;

	private JavaMailSenderImpl javaMailSender;

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			this.javaMailSender = new JavaMailSenderImpl();
			this.javaMailSender.setDefaultEncoding("UTF-8");
			this.javaMailSender.setHost(host);
			this.javaMailSender.setUsername(sender);
			this.javaMailSender.setPassword(passwd);

			Properties props = new Properties();
			// 这里要用字符串类型，不能用Boolean类型的true
			props.put("mail.smtp.auth", this.smtpAuth.toString());
			props.put("mail.smtp.timeout", this.smtpTimeout);
			this.javaMailSender.setJavaMailProperties(props);
		} catch (Exception e) {
			logger.error("EmailSenderService初始化失败.", e);
		}
	}

	@Override
	public void destroy() throws Exception {
		executor.shutdown();
	}

	/**
	 * 发送简单文本Email消息
	 * 
	 * @param emailMsg
	 */
	public void sendEmailMessageOfSimpleText(MailMessage emailMsg, Date date) {
		SimpleMailMessage simpleTextMessage = new SimpleMailMessage();
		simpleTextMessage.setFrom(sender);
		simpleTextMessage.setTo(emailMsg.getReceivers());
		if (emailMsg.getBcc() != null && emailMsg.getBcc().length > 0)
			simpleTextMessage.setBcc(emailMsg.getBcc());

		if (emailMsg.getCc() != null && emailMsg.getCc().length > 0)
			simpleTextMessage.setCc(emailMsg.getCc());

		simpleTextMessage.setText(emailMsg.getEmailContent());
		if (null == date)
			date = new Date();
		simpleTextMessage.setSentDate(date);

		javaMailSender.send(simpleTextMessage);
	}

	/**
	 * 带附件并简单文本格式邮件发送
	 *
	 * @param emailMsg
	 */
	public void sendEmailMessageOfHtmlText(MailMessage emailMsg, Date date) throws MessagingException {
		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(sender);
		helper.setTo(emailMsg.getReceivers());
		helper.setValidateAddresses(true);
		if (StringUtils.isNotBlank(emailMsg.getEmailContent()))
			helper.setText(emailMsg.getEmailContent(), true);

		helper.setSubject(emailMsg.getSubject());
		helper.setCc(emailMsg.getCc());
		helper.setBcc(emailMsg.getBcc());

		if (null == date)
			date = new Date();
		helper.setSentDate(date);

		javaMailSender.send(message);
	}

	/**
	 * 带附件并且html格式邮件发送,HTML格式的消息
	 * 
	 * @param emailMsg
	 * @param date
	 */
	public void sendEmailMessageOfAttachedFileAndSimpleText(MailMessage emailMsg, Date date, boolean isHtmlText)
			throws MessagingException {
		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);

		helper.setFrom(emailMsg.getSender());
		// helper.setValidateAddresses(true);
		helper.setText(emailMsg.getEmailContent(), isHtmlText);
		helper.setSubject(emailMsg.getSubject());
		helper.setCc(emailMsg.getCc());
		helper.setTo(emailMsg.getReceivers());
		helper.setBcc(emailMsg.getBcc());
		if (null == date)
			date = new Date();
		helper.setSentDate(date);

		for (File file : emailMsg.getAttachFile()) {
			FileSystemResource fileSystemResource = new FileSystemResource(file);
			helper.addAttachment(file.getName(), fileSystemResource);
		}
		javaMailSender.send(message);
	}

	/**
	 * 发送给系统管理员异常信息
	 * 
	 * @param msg
	 * @param t
	 * @return
	 * @sine
	 */
	public void sendAdminMessage(String msg, final Throwable t) {
		if (!this.enable) {
			logger.warn("未打开的系统消息email通知. (" + msg + ")");
			return;
		}

		final StringBuffer tempMsg = new StringBuffer("平台入口：");
		tempMsg.append(systemName);
		tempMsg.append("<br/>");
		tempMsg.append(msg);
		tempMsg.append("<br/>");
		try {
			if (t != null) {
				tempMsg.append(":\n");
				String tempErrMsg = ExceptionUtils.getStackTrace(t);
				if (tempErrMsg != null && tempErrMsg.length() > msglimit) {
					tempMsg.append(tempErrMsg.substring(0, msglimit));
				} else {
					tempMsg.append(tempErrMsg);
				}
			}
		} catch (Exception e) {
			logger.error("email通知系统消息到管理员异常.", e);
		}

		executor.submit(() -> {
			try {
				MailMessage emailMsg = new MailMessage();
				emailMsg.setSubject(systemName);
				emailMsg.setReceivers(emailAdminlist);
				emailMsg.setSender(null);
				emailMsg.setBcc(emailAdminlist);
				emailMsg.setCc(emailAdminlist);
				emailMsg.setEmailContent(tempMsg.toString());
				this.sendEmailMessageOfHtmlText(emailMsg, null);

			} catch (Throwable e) {
				logger.error("email通知系统消息到管理员失败.", e);
			}
		});
	}

	public static void main(String[] args) throws Exception {
		// MailMessage emailMsg = new MailMessage();
		// emailMsg.setReceivers(new String[] { "448797381@qq.com",
		// "1468433813@qq.com" });
		// emailMsg.setSubject("测试主题");
		// emailMsg.setBcc(new String[] { "448797381@qq.com",
		// "1468433813@qq.com" });
		// emailMsg.setCc(new String[] { "448797381@qq.com", "1468433813@qq.com"
		// });
		// emailMsg.setEmailContent("<html><body><h1>中国人民</h1><h5>测试<font
		// color=red>测试测试测</font>测试测试</h5></body></html>");
		//
		// MailSenderService es = new MailSenderService();
		// es.afterPropertiesSet();
		// es.sendEmailMessageOfHtmlText(emailMsg, null);
		// System.out.println("ok");
	}

}
