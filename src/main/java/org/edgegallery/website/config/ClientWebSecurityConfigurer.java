/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.edgegallery.website.config;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.edgegallery.website.common.Consts;
import org.edgegallery.website.sessionmgr.WebSocketSessionServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoRestTemplateFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableOAuth2Sso
public class ClientWebSecurityConfigurer extends WebSecurityConfigurerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientWebSecurityConfigurer.class);

    @Value("${AUTH_SERVER_ADDRESS}")
    private String authServerAddress;

    @Autowired
    private UserInfoRestTemplateFactory restTemplateFactory;

    @Autowired
    private DefaultTokenServices tokenService;

    @Autowired
    private TokenStore jwtTokenStore;

    @Autowired
    private ServletContext servletContext;

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/css/**", "/fonts/**", "/img/**", "/js/**", "/favicon.ico", "*.md");
    }

    @Override
    public void configure(final HttpSecurity http) throws Exception {
        http.headers().frameOptions().disable();
        http.authorizeRequests().antMatchers("/login", "/auth/logout").permitAll()
            // this api will be used by health-check service, so permit all roles to get mec host list in v1.2
            .antMatchers(HttpMethod.GET, "/mecm-inventory/inventory/v1/mechosts").permitAll()
            .antMatchers(HttpMethod.GET, "/health")
            .permitAll().antMatchers("/webssh").permitAll()
            .antMatchers("/wsserver/**").permitAll()
            .anyRequest()
            .authenticated().and()
            .addFilterBefore(oauth2ClientAuthenticationProcessingFilter(), BasicAuthenticationFilter.class).logout()
            .addLogoutHandler(new LogoutHandler() {
                @Override
                public void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                    Authentication authentication) {
                    HttpSession httpSession = httpServletRequest.getSession();
                    if (httpSession != null) {
                        WebSocketSessionServer.notifyHttpSessionInvalid(httpSession.getId(), Consts.HttpSessionInvalidScene.LOGOUT);
                    }
                }
            })
            .logoutUrl("/logout").logoutSuccessUrl(authServerAddress + "/auth/logout")
            .and().csrf()
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
    }

    /**
     * oauth2ClientAuthenticationProcessingFilter.
     */
    @Bean
    public OAuth2ClientAuthenticationProcessingFilter oauth2ClientAuthenticationProcessingFilter() {
        OAuth2ClientAuthenticationProcessingFilter filter = new OAuth2ClientAuthenticationProcessingFilter("/login");
        filter.setRestTemplate(restTemplateFactory.getUserInfoRestTemplate());
        filter.setTokenServices(tokenService);
        filter.setAuthenticationSuccessHandler(new SimpleUrlAuthenticationSuccessHandler() {
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                Authentication authentication) throws IOException, ServletException {
                HttpSession session = request.getSession(false);
                OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();
                Map<String, Object> additionalInformation = jwtTokenStore.readAccessToken(details.getTokenValue())
                    .getAdditionalInformation();
                servletContext.setAttribute(additionalInformation.get("ssoSessionId").toString(), session);
                super.onAuthenticationSuccess(request, response, authentication);
            }
        });
        return filter;
    }

    /**
     * requestForwardFilter.
     */
    @Bean
    public ZuulFilter requestForwardFilter() {
        return new ZuulFilter() {
            @Override
            public String filterType() {
                return FilterConstants.PRE_TYPE;
            }

            @Override
            public int filterOrder() {
                return FilterConstants.SEND_FORWARD_FILTER_ORDER;
            }

            @Override
            public boolean shouldFilter() {
                return true;
            }

            @Override
            public Object run() {
                try {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();
                    String accessToken = details.getTokenValue();
                    RequestContext ctx = RequestContext.getCurrentContext();
                    ctx.addZuulRequestHeader("access_token", accessToken);
                } catch (Exception e) {
                    LOGGER.warn("there will be a exception when permit all roles to access the api. no need to fix it.");
                    return null;
                }
                return null;
            }
        };
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
