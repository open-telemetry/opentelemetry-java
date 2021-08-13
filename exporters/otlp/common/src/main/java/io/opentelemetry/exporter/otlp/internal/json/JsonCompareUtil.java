package io.opentelemetry.exporter.otlp.internal.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JsonCompareUtil {

    public static Boolean containsExactly(JSONArray actualJsonArray, JSONObject exceptedJson) {
        List<String> resultList = new LinkedList<>();
        for (int i = 0; i < actualJsonArray.size(); i++) {
            if (verifyJsonObject(actualJsonArray.getString(i)) && verifyJsonObject(exceptedJson.toString())) {
                if (compareJsonObject(actualJsonArray.getJSONObject(i), exceptedJson)) {
                    resultList.add(String.valueOf(i));
                }
            } else {
                if (actualJsonArray.get(i).equals(exceptedJson)) {
                    resultList.add(String.valueOf(i));
                }
            }
        }
        return resultList.size() > 0;
    }

    public static Boolean containsExactly(JSONArray actualJsonArray, JSONArray exceptedJsonArray) {
        List<String> resultList = new LinkedList<>();
        for (int i = 0; i < exceptedJsonArray.size(); i++) {
            if (verifyJsonObject(actualJsonArray.getString(i)) && verifyJsonObject(exceptedJsonArray.getString(i))) {
                if (compareJsonObject(actualJsonArray.getJSONObject(i), exceptedJsonArray.getJSONObject(i))) {
                    resultList.add(String.valueOf(i));
                }
            } else {
                if (actualJsonArray.get(i).equals(exceptedJsonArray.get(i))) {
                    resultList.add(String.valueOf(i));
                }
            }
        }
        return resultList.size() > 0;
    }

    public static Boolean compareJsonObject(JSONObject actualJson, JSONObject exceptedJson) {
        List<String> resultList = new LinkedList<>();
        for (Map.Entry<String, Object> actualEntry : actualJson.entrySet()) {
            for (Map.Entry<String, Object> exceptedEntry : exceptedJson.entrySet()) {
                if (actualEntry.getKey().equals(exceptedEntry.getKey())) {
                    if (!actualEntry.getValue().toString().equals(exceptedEntry.getValue().toString())) {
                        resultList.add(actualEntry.getKey());
                    }
                }
            }
        }
        return resultList.size() <= 0;
    }

    private static Boolean verifyJson(String jsonString) {
        try {
            JSON.parse(jsonString);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    public static Boolean verifyJsonObject(String jsonString) {
        if (!verifyJson(jsonString)) {
            return false;
        }
        try {
            JSONObject.parseObject(jsonString);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    JsonCompareUtil() {}
}
