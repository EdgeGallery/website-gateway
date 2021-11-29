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

package org.edgegallery.website.common;

/**
 * constant define.
 */
public class Consts {
    /**
     * http session timeout.
     */
    public static final long HTTP_SESSION_TIMEOUT = 3600;
    /**
     * advance notify time for http session timeout.
     */
    public static final long ADV_NOTIFY_TIME_FOR_HTTP_SESSION_TIMEOUT = 60;
    /**
     * request header for access token.
     */
    public static final String HEADER_ACCESS_TOKEN = "access_token";
    /**
     * north api request header for access token.
     */
    public static final String HEADER_NORTHAPI_ACCESS_TOKEN = "N_ACCESS_TOKEN";
    /**
     * http session invalid scene.
     */
    public static final class HttpSessionInvalidScene {
        private HttpSessionInvalidScene() {
        }

        /**
         * timeout.
         */
        public static final int TIMEOUT = 1;
        /**
         * logout.
         */
        public static final int LOGOUT = 2;
        /**
         * server stopped.
         */
        public static final int SERVER_STOP = 3;
    }
}
