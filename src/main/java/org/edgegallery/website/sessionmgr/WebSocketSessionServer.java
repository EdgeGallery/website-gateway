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

package org.edgegallery.website.sessionmgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import org.edgegallery.website.common.Consts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Websocket Session Server.
 */
@ServerEndpoint("/wsserver/{httpSessionId}")
@Component
public class WebSocketSessionServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketSessionServer.class);

    private static final Map<String, List<Session>> WS_SESSION_FINDER = new HashMap<>();

    private static final Map<String, String> HTTP_SESSIONID_FINDER = new HashMap<>();

    private static final Object LOCK_OBJ = new Object();

    /**'
     * open websocket session.
     *
     * @param wsSession websocket session
     * @param httpSessionId http session id
     */
    @OnOpen
    public void onOpen(Session wsSession, @PathParam("httpSessionId") String httpSessionId) {
        if (StringUtils.isEmpty(httpSessionId)) {
            LOGGER.warn("invalid http session id.");
            return;
        }

        LOGGER.debug("ws client opened.");
        synchronized (LOCK_OBJ) {
            List<Session> wsSessList = WS_SESSION_FINDER.get(httpSessionId);
            if (wsSessList == null) {
                wsSessList = new ArrayList<>();
                WS_SESSION_FINDER.put(httpSessionId, wsSessList);
            }

            wsSessList.add(wsSession);
            HTTP_SESSIONID_FINDER.put(wsSession.getId(), httpSessionId);
        }
    }

    /**
     * receive message from ws client.
     *
     * @param message message
     * @param wsSession websocket session
     */
    @OnMessage
    public void onMessage(String message, Session wsSession) {
        LOGGER.debug("receive message from ws client");
        synchronized (LOCK_OBJ) {
            String httpSessId = HTTP_SESSIONID_FINDER.get(wsSession.getId());
            HttpSession httpSession = CustomHttpSessionManager.getInstance().getSession(httpSessId);
            if (httpSession != null) {
                if ((System.currentTimeMillis() - httpSession.getLastAccessedTime()) / 1000
                    > Consts.HTTP_SESSION_TIMEOUT - Consts.ADV_NOTIFY_TIME_FOR_HTTP_SESSION_TIMEOUT) {
                    notifyHttpSessionInvalid(httpSessId, Consts.HttpSessionInvalidScene.TIMEOUT);
                }
            }
        }
    }

    /**
     * notify http session invalid.
     *
     * @param httpSessionId Http Session Id
     * @param invalidScene Session invalidation scene
     */
    public static void notifyHttpSessionInvalid(String httpSessionId, int invalidScene) {
        synchronized (LOCK_OBJ) {
            List<Session> wsSessList = WS_SESSION_FINDER.get(httpSessionId);
            if (wsSessList == null) {
                return;
            }
            wsSessList.forEach(wsSession -> {
                LOGGER.info("notify http session timeout. wsId = {}", wsSession.getId());
                try {
                    wsSession.getBasicRemote().sendText(String.valueOf(invalidScene));
                } catch (Exception e) {
                    LOGGER.error("notify failed: {}", e.getMessage());
                }
                HTTP_SESSIONID_FINDER.remove(wsSession.getId());
            });
            WS_SESSION_FINDER.remove(httpSessionId);
        }
    }
}
