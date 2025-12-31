package com.smart.edilek.models;

import lombok.Data;

@Data
public class PageViewRequest {
    private String path;
    private String referrer;
    private String userAgent;
}
