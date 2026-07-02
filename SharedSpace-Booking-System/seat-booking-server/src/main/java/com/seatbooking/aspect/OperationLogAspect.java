package com.seatbooking.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seatbooking.context.UserContext;
import com.seatbooking.entity.OperationLog;
import com.seatbooking.mapper.OperationLogMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Around("@annotation(com.seatbooking.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        com.seatbooking.annotation.OperationLog annotation = method.getAnnotation(com.seatbooking.annotation.OperationLog.class);

        OperationLog opLog = new OperationLog();
        opLog.setOperation(annotation.value());
        opLog.setMethod(method.getDeclaringClass().getSimpleName() + "." + method.getName());

        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                try {
                    opLog.setParams(objectMapper.writeValueAsString(args));
                } catch (Exception e) {
                    opLog.setParams("参数序列化失败");
                }
            }
        } catch (Exception e) {
            opLog.setParams("无法获取参数");
        }

        Long userId = UserContext.getCurrentUserId();
        String username = UserContext.getCurrentUsername();
        opLog.setUserId(userId);
        opLog.setUsername(username);

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                opLog.setIp(getIpAddr(request));
            }
        } catch (Exception e) {
            opLog.setIp("未知");
        }

        Object result;
        try {
            result = joinPoint.proceed();
            opLog.setStatus(1);
            return result;
        } catch (Throwable e) {
            opLog.setStatus(0);
            opLog.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            opLog.setCostTime(costTime);
            opLog.setCreateTime(LocalDateTime.now());
            opLog.setDeleted(0);
            try {
                operationLogMapper.insert(opLog);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
