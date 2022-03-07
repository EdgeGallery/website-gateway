/*
 *    Copyright 2020-2022 Huawei Technologies Co., Ltd.
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

package org.edgegallery.website.controller;

import io.swagger.annotations.ApiOperation;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.website.model.LoginInfoRespDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Slf4j
@RestSchema(schemaId = "auth")
@RequestMapping("/auth")
public class OAuthClientController {
    @Autowired
    JwtServer jwtServer;

    @Autowired
    private ServletContext servletContext;

    @Value("${AUTH_SERVER_ADDRESS_CLIENTACCESS}")
    private String authServerAddressClientAccess;

    @Value("${IS_SECURE_BACKEND}")
    private String isSecureBackend;

    /**
     * getLoginInfo.
     */
    @RequestMapping(value = "/login-info", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "get user information", response = LoginInfoRespDto.class,
        notes = "The API can " + "receive the get user information request")
    public ResponseEntity<LoginInfoRespDto> getLoginInfo(HttpServletRequest request) {
        OAuth2AuthenticationDetails details = jwtServer.getAuthDetails();
        Map<String, Object> additionalInformation = jwtServer.getToken(details.getTokenValue())
            .getAdditionalInformation();
        LoginInfoRespDto loginInfoRespDto = new LoginInfoRespDto();
        loginInfoRespDto.setUserId(additionalInformation.get("userId"));
        loginInfoRespDto.setAccessToken(details.getTokenValue());
        loginInfoRespDto.setIsSecureBackend(isSecureBackend);
        loginInfoRespDto.setUserName(additionalInformation.get("userName"));

        StringBuilder loginPage = new StringBuilder(authServerAddressClientAccess);
        loginPage.append("/index.html?enable_sms=")
            .append(additionalInformation.get("enableSms"))
            .append("&enable_mail=")
            .append(additionalInformation.get("enableMail"))
            .append("&enable_external_iam=")
            .append(additionalInformation.get("enableExternalIam"));
        loginInfoRespDto.setLoginPage(loginPage.toString());

        loginInfoRespDto.setUserCenterPage(authServerAddressClientAccess + "/index.html#/usermgmt/center");
        if (additionalInformation.containsKey("pwmodiscene")) {
            loginInfoRespDto.setForceModifyPwPage(authServerAddressClientAccess + "/index.html#/usermgmt/forcemodifypwd");
        }
        loginInfoRespDto.setAuthorities(additionalInformation.get("authorities"));
        loginInfoRespDto.setSessId(request.getSession().getId());
        return new ResponseEntity<>(loginInfoRespDto, HttpStatus.OK);
    }

    /**
     * logout.
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "logout", response = String.class, notes = "Logout by global sessionId")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String ssoSessionId = request.getParameter("ssoSessionId");
        HttpSession session = (HttpSession) servletContext.getAttribute(ssoSessionId);
        if (session != null) {
            try {
                session.invalidate();
            } catch (IllegalStateException e) {
                log.info("The session already invalid.");
            }
            servletContext.removeAttribute(ssoSessionId);
        }
        return new ResponseEntity<>("Succeed", HttpStatus.OK);
    }
}
