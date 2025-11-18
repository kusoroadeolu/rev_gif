package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.configprops.CookieConfigProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
@Component
@Slf4j
public class CookieUtils {

    private final CookieConfigProperties configProperties;

    public Cookie getCookie(HttpServletRequest request){
        final Cookie existing = this.findCookie(request);
        return existing == null ? this.generateCookie() : existing;
    }

    public void addCookie(HttpServletRequest request, HttpServletResponse response, @NonNull Cookie cookie){
        final Cookie existing = this.findCookie(request);
        if(existing == null) response.addCookie(cookie);
        ExecutorService s = Executors.newVirtualThreadPerTaskExecutor();
    }

    private Cookie findCookie(HttpServletRequest request){
        return WebUtils.getCookie(request, this.configProperties.name());
    }

    public Cookie generateCookie(){
        final UUID sesh = UUID.randomUUID();
        final Cookie cookie = new Cookie(this.configProperties.name(), sesh.toString());
        cookie.setPath(this.configProperties.path());
        cookie.setHttpOnly(this.configProperties.httpOnly());
        cookie.setMaxAge(this.configProperties.maxAge());
        cookie.setSecure(this.configProperties.secure());
        return cookie;
    }

}
