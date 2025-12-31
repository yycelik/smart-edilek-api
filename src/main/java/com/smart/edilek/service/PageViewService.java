package com.smart.edilek.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.smart.edilek.core.service.GenericServiceImp;
import com.smart.edilek.entity.PageView;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class PageViewService {

    @Autowired
    private GenericServiceImp<PageView> pageViewGenericService;

    @Async
    public void save(String path, String referrer, String userAgent, HttpServletRequest request) {
        PageView pageView = new PageView();
        pageView.setPath(path);
        pageView.setReferrer(referrer);
        pageView.setUserAgent(userAgent);
        pageView.setIpAddress(getClientIp(request));
        pageView.setViewTime(LocalDateTime.now());

        try {
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            if (userId != null && !userId.equals("anonymousUser")) {
                pageView.setUserId(userId);
            }
        } catch (Exception e) {
            // Ignore if security context is not available
        }

        try {
            pageViewGenericService.add(pageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        return xf != null ? xf.split(",")[0] : request.getRemoteAddr();
    }
}
