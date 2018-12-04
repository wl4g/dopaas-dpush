package io.transport.common;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 以静态变量保存Spring ApplicationContext, 可在任何代码任何地方任何时候取出ApplicaitonContext.
 * 
 * @author wangl.sir
 * @version v1.0 2016-3-9 10:45:50
 * @since wangl.sir Automatic java code generator.
 */
@Component
@Lazy(false)
public class SpringContextHolder implements ApplicationContextAware, DisposableBean {
	final private static Logger logger = LoggerFactory.getLogger(SpringContextHolder.class);
	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		if (applicationContext != null)
			logger.info("ApplicationContext is covered, original applicationContext={}", applicationContext);
		SpringContextHolder.applicationContext = applicationContext;
	}

	/**
	 * 实现DisposableBean接口, 在Context关闭时清理静态变量.
	 */
	@Override
	public void destroy() throws Exception {
		SpringContextHolder.applicationContext = null;
	}

	/**
	 * 从静态变量applicationContext中取得Bean, 自动转型为所赋值对象的类型.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String name) {
		Validate.validState(applicationContext != null, "applicaitonContext属性未注入.");

		return (T) applicationContext.getBean(name);
	}

	/**
	 * 从静态变量applicationContext中取得Bean, 自动转型为所赋值对象的类型.
	 */
	public static <T> T getBean(Class<T> requiredType) {
		Validate.validState(applicationContext != null, "applicaitonContext属性未注入.");
		return applicationContext.getBean(requiredType);
	}

	/**
	 * 从静态变量applicationContext中取得Bean, 自动转型为所赋值对象的类型列表.
	 * 
	 * @param requiredType
	 * @return
	 */
	public static <T> Map<String, T> getBeansOfType(Class<T> requiredType) {
		Validate.validState(applicationContext != null, "applicaitonContext属性未注入.");
		return applicationContext.getBeansOfType(requiredType);
	}

}