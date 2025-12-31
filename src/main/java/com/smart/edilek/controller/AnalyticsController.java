package com.smart.edilek.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smart.edilek.models.PageViewRequest;
import com.smart.edilek.service.PageViewService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final PageViewService service;

    public AnalyticsController(PageViewService service) {
        this.service = service;
    }

    @PostMapping("/page-view")
    public void track(@RequestBody PageViewRequest request, HttpServletRequest httpRequest) {
        service.save(
            request.getPath(),
            request.getReferrer(),
            request.getUserAgent(),
            httpRequest
        );
    }
}
