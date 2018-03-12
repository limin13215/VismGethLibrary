package com.vism.gethlibrary;

import org.json.JSONObject;

/**
 * Created by Administrator on 2018-3-12.
 */

public class JsonTools {
    private static final String STATUS_0 = "0";
    private static final String STATUS_1 = "1";
    /**
     *   解析json字符串， 获取资产余额字段
     * @param jsonData
     */
    public static String parseJSONWithJSONObject(String jsonData){
        String result = "";
        String status = "";
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            status = jsonObject.getString("status");
            if (status.equals(STATUS_0)){
                result = "查询失败";
            }else {
                result = jsonObject.getString("result");
            }
        } catch (Exception e) {
            return "查询失败";
        }
        return result;
    }
}
