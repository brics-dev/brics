package gov.nih.cit.brics.file.configuration;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.TimeoutCallableProcessingInterceptor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfiguration implements AsyncConfigurer {
	private final Logger logger = LoggerFactory.getLogger(AsyncConfiguration.class);

	@Autowired
	private Environment env;

	@Override
	@Bean(name = "taskExecutor")
	public AsyncTaskExecutor getAsyncExecutor() {
		logger.info("Creating Async Task Executor");
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

		executor.setCorePoolSize(Integer.parseInt(env.getProperty("fileRepo.taskExecutor.pool.size.core")));
		executor.setMaxPoolSize(Integer.parseInt(env.getProperty("fileRepo.taskExecutor.pool.size.max")));
		executor.setQueueCapacity(Integer.parseInt(env.getProperty("fileRepo.taskExecutor.queue.capacity")));

		return executor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new SimpleAsyncUncaughtExceptionHandler();
	}

	/** Configure async support for Spring MVC. */
	@Bean
	public WebMvcConfigurer webMvcConfigurerConfigurer(AsyncTaskExecutor taskExecutor,
			CallableProcessingInterceptor callableProcessingInterceptor) {
		return new WebMvcConfigurer() {
			@Override
			public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
				configurer
						.setDefaultTimeout(
								Long.parseLong(env.getProperty("fileRepo.taskExecutor.timeout.milliseconds")))
						.setTaskExecutor(taskExecutor);
				configurer.registerCallableInterceptors(callableProcessingInterceptor);
				WebMvcConfigurer.super.configureAsyncSupport(configurer);
			}
		};
	}

	@Bean
	public CallableProcessingInterceptor callableProcessingInterceptor() {
		return new TimeoutCallableProcessingInterceptor() {
			@Override
			public <T> Object handleTimeout(NativeWebRequest request, Callable<T> task) throws Exception {
				logger.error("timeout!");
				return super.handleTimeout(request, task);
			}
		};
	}

}
