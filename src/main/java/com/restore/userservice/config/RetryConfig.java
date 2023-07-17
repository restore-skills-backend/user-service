package com.restore.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RetryConfig {

    private static final Long initialInterval = 100L;
    private static final Integer maxAttempts = 10;

    @Bean
    public RetryTemplate createRetryOnOptimisticLockTemplate() {
        return createRetryTemplate(ObjectOptimisticLockingFailureException.class);
    }

    @Bean
    public RetryTemplate createRetryTemplate(Class<? extends Throwable> ex) {
        Map<Class<? extends Throwable>, Boolean> exceptions = new HashMap<>();
        exceptions.put(ex, true);

        RetryTemplate template = new RetryTemplate();
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(maxAttempts, exceptions);
        template.setRetryPolicy(retryPolicy);

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialInterval);
        template.setBackOffPolicy(backOffPolicy);

        return template;
    }
}
