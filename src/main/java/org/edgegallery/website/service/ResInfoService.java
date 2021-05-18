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

package org.edgegallery.website.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service("ResInfoService")
public class ResInfoService {
    @Value("${website-gateway.res-path}")
    private String dir;

    private static String ERRINFO_EN_PATH = "/i18n/en_US/%s/errorCode.properties";

    private static String ERRINFO_ZH_PATH = "/i18n/zh_CN/%s/errorCode.properties";

    /**
     * get error infos from resource.
     *
     */
    public ResponseEntity<String> getErrInfo(String modules) {
        Map<String, Properties> local2Properties = new ConcurrentHashMap<>();
        List<String> moduleList = new Gson().fromJson(modules, new TypeToken<List<String>>(){}.getType());
        // add common error resource info
        moduleList.add("common");
        for (String module : moduleList) {
            String enFile = dir + String.format(ERRINFO_EN_PATH, module);
            String zhFile = dir + String.format(ERRINFO_ZH_PATH, module);
            parseFile(enFile, local2Properties, "en_US");
            parseFile(zhFile, local2Properties, "zh_CN");
        }
        return ResponseEntity.ok(JSONObject.toJSONString(local2Properties));
    }

    /**
     * parse properties file.
     *
     */
    public void parseFile(String filePath, Map<String, Properties> local2Properties, String lan) {
        try {
            InputStreamReader in = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8);
            Properties properties = new Properties();
            properties.load(in);
            local2Properties.computeIfAbsent(lan, key -> new Properties()).putAll(properties);
        } catch (IOException | RuntimeException e) {
            log.error("parse file catch exception: {}", e.getMessage());
        }
    }
}
