package com.unilead;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.*;

public class Json {
    public static Map<String, String> jsonStringToMap(String jsonParams) throws Exception {
        Map<String, String> jsonMap = new HashMap<String, String>();

        if (jsonParams == null || jsonParams.equals("")) return jsonMap;

        JSONObject jsonObject = (JSONObject) new JSONTokener(jsonParams).nextValue();
        Iterator<?> keys = jsonObject.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            jsonMap.put(key, jsonObject.getString(key));
        }

        return jsonMap;
    }

    public static String mapToJsonString(Map<String, String> map) {
        if (map == null) {
            return "{}";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("{");
        boolean first = true;

        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!first) {
                builder.append(",");
            }
            builder.append("\"");
            builder.append(entry.getKey());
            builder.append("\":\"");
            builder.append(entry.getValue());
            builder.append("\"");
            first = false;
        }

        builder.append("}");
        return builder.toString();
    }

    public static String[] jsonArrayToStringArray(String jsonString) {
        jsonString = "{key:" + jsonString + "}";

        try {
            JSONObject jsonObject = (JSONObject) new JSONTokener(jsonString).nextValue();
            JSONArray jsonArray = jsonObject.getJSONArray("key");

            String[] result = new String[jsonArray.length()];
            for (int i = 0; i < result.length; i++) {
                result[i] = jsonArray.getString(i);
            }

            return result;
        } catch (JSONException exception) {
            return new String[0];
        }
    }

}
