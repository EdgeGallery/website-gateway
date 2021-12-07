/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.website;

import static org.mockito.Mockito.mock;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import org.edgegallery.website.controller.JwtServer;
import org.edgegallery.website.controller.OAuthClientController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(MockitoJUnitRunner.class)
public class TestOAuthClientController {

    protected MockMvc mvc;

    @InjectMocks
    protected OAuthClientController oAuthClientController;

    @Mock
    protected JwtServer jwtServer;

    @Mock
    private ServletContext servletContext;

    @Before
    public void setUp() {
        this.mvc = MockMvcBuilders.standaloneSetup(oAuthClientController).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void should_successfully_when_get_login_info() throws Exception {
        ReflectionTestUtils.setField(oAuthClientController, "authServerAddressClientAccess", "http://127.0.0.1:8080");
        OAuth2AccessToken token = new OAuth2AccessToken() {
            @Override
            public Map<String, Object> getAdditionalInformation() {
                return new HashMap<>();
            }

            @Override
            public Set<String> getScope() {
                return null;
            }

            @Override
            public OAuth2RefreshToken getRefreshToken() {
                return null;
            }

            @Override
            public String getTokenType() {
                return null;
            }

            @Override
            public boolean isExpired() {
                return false;
            }

            @Override
            public Date getExpiration() {
                return null;
            }

            @Override
            public int getExpiresIn() {
                return 0;
            }

            @Override
            public String getValue() {
                return null;
            }
        };
        Mockito.when(jwtServer.getToken(Mockito.isNull())).thenReturn(token);
        Mockito.when(jwtServer.getAuthDetails()).thenReturn(mock(OAuth2AuthenticationDetails.class));
        mvc.perform(MockMvcRequestBuilders.get("/auth/login-info").contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    public void should_successfully_when_logout() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/auth/logout").contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("ssoSessionId", "test_sso_session_id").accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
