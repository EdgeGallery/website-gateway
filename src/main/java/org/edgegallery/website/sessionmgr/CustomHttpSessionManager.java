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

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpSession;

/**
 * Http Session Manager.
 */
public final class CustomHttpSessionManager {
    private static final CustomHttpSessionManager INSTANCE = new CustomHttpSessionManager();

    private static final Map<String, HttpSession> SESSION_MAP = new HashMap<>();

    private static final Object LOCK_OBJ = new Object();

    private CustomHttpSessionManager() {}

    /**
     * get single instance.
     *
     * @return HttpSessionManager Instance
     */
    public static CustomHttpSessionManager getInstance() {
        return INSTANCE;
    }

    /**
     * add session.
     *
     * @param httpSession Http Session
     */
    public static void addSession(HttpSession httpSession) {
        synchronized (LOCK_OBJ) {
            SESSION_MAP.put(httpSession.getId(), httpSession);
        }
    }

    /**
     * remove session.
     *
     * @param httpSession Http Session
     */
    public void removeSession(HttpSession httpSession) {
        synchronized (LOCK_OBJ) {
            SESSION_MAP.remove(httpSession.getId());
        }
    }

    /**
     * get session by id.
     *
     * @param httpSessionId Http Session Id
     * @return Http Session
     */
    public HttpSession getSession(String httpSessionId) {
        synchronized (LOCK_OBJ) {
            return SESSION_MAP.get(httpSessionId);
        }
    }
}
