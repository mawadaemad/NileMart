package org.route;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class ExecutionLog {

    @Around("execution(* org.route.*Service.*(..))")
    public Object executeLog(ProceedingJoinPoint jp) throws Throwable {
        String op = jp.getSignature().toShortString();
        System.out.println(op + " Started");
        long start = System.nanoTime();
        try {
            Object result = jp.proceed();
            System.out.println(op + " Success (" + elapsedMs(start) + " ms)");
            return result;
        } catch (Throwable e) {
            System.out.println(op + " Failed (" + elapsedMs(start) + " ms)");
            throw e;
        }
    }

    private static long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
