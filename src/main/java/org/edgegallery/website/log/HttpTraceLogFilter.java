/*
 *  Copyright 2020-2022 Huawei Technologies Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.edgegallery.website.log;

import com.google.gson.Gson;
import java.io.IOException;
import java.time.LocalDateTime;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class HttpTraceLogFilter extends OncePerRequestFilter implements Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpTraceLogFilter.class);

    private static final String[] urlPatterns = {
        "/login", "/logout", "/auth/", "/mec-appstore/", "/mec-developer/", "/mec-atp/", "/mec-lab/",
        "/mecm-inventory/", "/mecm-appo/", "/mecm-apm/", "/mecm-north/", "/mec-thirdsystem/", "/mec-usermgmt/", "/app-mgmt/"
    };

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String url = request.getRequestURI();
        if (!StringUtils.startsWithAny(url, urlPatterns)) {
            filterChain.doFilter(request, response);
            return;
        }
        HttpRequestLog requestLog = new HttpRequestLog();
        try {
            requestLog.setRequestLog(logForRequest(request));
            filterChain.doFilter(request, response);
        } finally {
            requestLog.setResponseLog(logForResponse(response));
            LOGGER.info("Http trace log: {}", new Gson().toJson(requestLog));
        }
    }

    private HttpRequestTraceLog logForRequest(HttpServletRequest request) {
        HttpRequestTraceLog requestTraceLog = new HttpRequestTraceLog();
        requestTraceLog.setTime(LocalDateTime.now().toString());
        requestTraceLog.setPath(request.getRequestURI());
        requestTraceLog.setMethod(request.getMethod());
        requestTraceLog.setIp(getIpAddress(request));
        return requestTraceLog;
    }

    private HttpResponseTraceLog logForResponse(HttpServletResponse response) {
        HttpResponseTraceLog responseTraceLog = new HttpResponseTraceLog();
        responseTraceLog.setStatus(response.getStatus());
        responseTraceLog.setTime(LocalDateTime.now().toString());
        return responseTraceLog;
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // the first IP is the real IP
        if (!StringUtils.isEmpty(ip) && ip.indexOf(",") > 0) {
            ip = ip.substring(0, ip.indexOf(","));
        }
        return ip;
    }

    @Setter
    @Getter
    private static class HttpRequestLog {
        HttpRequestTraceLog requestLog;

        HttpResponseTraceLog responseLog;
    }

    @Setter
    @Getter
    private static class HttpRequestTraceLog {
        private String path;

        private String userId;

        private String method;

        private String time;

        private String requestBody;

        private String ip;
    }

    @Setter
    @Getter
    private static class HttpResponseTraceLog {
        private Integer status;

        private String time;
    }
}
