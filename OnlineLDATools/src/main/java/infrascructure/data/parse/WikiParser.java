/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package infrascructure.data.parse;

import infrascructure.data.PlainTextResource;
import infrascructure.data.Resource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author shredinger
 */
public class WikiParser implements Parser {

    /* (non-Javadoc)
     * @see infrascructure.data.Parser#parse(infrascructure.data.Resource)
     */
    @Override
    public PlainTextResource parse(Resource r) {

        String html = r.getData();
        int fromIndex = 0;


        return null;
    }

    private int findDiv(String source, String id) {
        String reg = "<div(\\s)+id(\\s)*=(\\s)*\"" + id + "\".*>";
        Pattern pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(source);
        int startIndex = -1;
        if (matcher.find()) {
            startIndex = matcher.start();
        }

        return startIndex;
    }

}
