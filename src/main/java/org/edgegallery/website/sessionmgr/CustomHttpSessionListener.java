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

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Http Session Listener.
 */
@WebListener
public class CustomHttpSessionListener implements HttpSessionListener {
    /**
     * listen session created.
     *
     * @param se session created event
     */
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        CustomHttpSessionManager.getInstance().addSession(se.getSession());
    }

    /**
     * listen session destroyed.
     *
     * @param se session destroyed event
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        CustomHttpSessionManager.getInstance().removeSession(se.getSession());
    }
}
