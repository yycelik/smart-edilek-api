package com.smart.edilek.core.component;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MethodExecutionTimeAspect {

    private static final Logger logger = LoggerFactory.getLogger(MethodExecutionTimeAspect.class);

    @Value("${logging.executionTime.enabled:false}")
    private boolean isExecutionTimeLoggingEnabled;

    @Around("@annotation(com.smart.edilek.core.annotation.LogExecutionTime) || @within(com.smart.edilek.core.annotation.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!isExecutionTimeLoggingEnabled) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();

        Object proceed = joinPoint.proceed();

        long executionTime = System.currentTimeMillis() - startTime;

        logger.info("{} executed in {} ms", joinPoint.getSignature(), executionTime);

        return proceed;
    }
}