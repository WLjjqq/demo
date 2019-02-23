package com.hlw.demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.hlw.demo.bean.AccessToken;
import com.hlw.demo.bean.ResultTemplateDate;
import com.hlw.demo.bean.User;
import com.hlw.demo.service.UserService;
import com.hlw.demo.util.JsonResult;
import com.hlw.demo.util.ResultCode;
import com.hlw.demo.util.WebChatUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
public class UserController {
    @Autowired
    UserService userService;

    @RequestMapping("/getUser/{userId}")
    @ResponseBody
    public User getUser(@PathVariable("userId") Integer userId) throws IOException{
        User user = userService.getUser(userId);
        if (user !=null){
            PushWX();
        }
        return user;
    }

    public  JsonResult PushWX() throws IOException {
        String params = generatePushTemplate();
        if (params.equals("")) {
            return new JsonResult(ResultCode.EXCEPTION, "generatePushTemplate()失败");
        }

        ArrayList<String> sendSuccessUsers = new ArrayList<String>();
        ArrayList<String> sendFailUsers = new ArrayList<String>();

        String pushMessageUrl = WebChatUtil.SEND_TEMPLAYE_MESSAGE_URL + getAccessToken();
        HttpPost httpPost = new HttpPost(pushMessageUrl);
        CloseableHttpClient httpClient = HttpClients.createDefault();

        String openid="o7d6O1jY0FEE-qGik-J1YqUwLGRk"; //微信用户的id，就是是哪个人关注这个公众号
        // 装配post请求参数
        StringBuffer buffer = new StringBuffer();
        // 按照官方api的要求提供params
        buffer.append("{");
        buffer.append(String.format("\"touser\":\"%s\"", openid)).append(",");
        buffer.append(params);
        String urlParams = new String(buffer.toString().getBytes("UTF-8"));

        StringEntity myEntity = new StringEntity(urlParams, ContentType.APPLICATION_JSON);
        System.out.println("Push Message urlParams = " + urlParams);

        httpPost.setEntity(myEntity);
        HttpResponse httpResponse = httpClient.execute(httpPost);
        String resultStr = "发送失败";

        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            // 发送成功
            String resutlEntity = EntityUtils.toString(httpResponse.getEntity());
            ResultTemplateDate resultTemplateDate = JSONObject.parseObject(resutlEntity, ResultTemplateDate.class);
            if (resultTemplateDate.getErrcode().equals("40037")) {
                resultStr = "template_id不正确";
                sendFailUsers.add(openid);
            }
            if (resultTemplateDate.getErrcode().equals("41028")) {
                resultStr = "form_id不正确，或者过期";
                sendFailUsers.add(openid);
            }
            if (resultTemplateDate.getErrcode().equals("41029")) {
                resultStr = "form_id已被使用";
                sendFailUsers.add(openid);
            }
            if (resultTemplateDate.getErrcode().equals("41030")) {
                resultStr = "page不正确";
                sendFailUsers.add(openid);
            }
            if (resultTemplateDate.getErrcode().equals("45009")) {
                resultStr = "接口调用超过限额（目前默认每个帐号日调用限额为100万）";
                sendFailUsers.add(openid);
            }
            sendSuccessUsers.add(openid);
        } else {
            // 发送失败
            sendFailUsers.add(openid);
        }

        JsonResult jsonResult = new JsonResult();
        if (sendFailUsers.isEmpty()) {
            jsonResult.setCode(ResultCode.SUCCESS);
            jsonResult.setMessage("全部USER通知发送成功");
        }else {
            jsonResult.setCode(ResultCode.FAIL);
            jsonResult.setMessage("USER微信通知发送失败");
        }
        HashMap<String, Object> sendMessageResult = new HashMap<String, Object>();
        sendMessageResult.put("sendSuccessUsers", sendSuccessUsers);
        sendMessageResult.put("sendFailUsers", sendFailUsers);
        jsonResult.setData(sendMessageResult);
        return jsonResult;
    }
    private String generatePushTemplate() {
        String template_id = "frP4y7kwVNELCvV6zNWD6C6kLrKonuWebwUnX-bYvt0"; //模板id
        String url="www.baidu.com";  //跳转的url
        String firstContent ="2019年全国XXX业余大赛";
        String firstColor = "#173177";

        String keyword_1_Content = "2019年XXX业余大赛将于北京举行";
        String keyword_1_Color = "#FF0000";

        String keyword_2_Content = "欢迎报名参加！";
        String keyword_2_Color = "#173177";

        StringBuffer buffer = new StringBuffer();
        buffer.append(String.format("\"template_id\":\"%s\"", template_id)).append(",");
        buffer.append(String.format("\"url\":\"%s\"", url)).append(",");
        buffer.append("\"data\":{");
        buffer.append(String.format("\"%s\": {\"value\":\"%s\",\"color\":\"%s\"},", "first", firstContent, firstColor));
        buffer.append(String.format("\"%s\": {\"value\":\"%s\",\"color\":\"%s\"},", "keyword1", keyword_1_Content,
                keyword_1_Color));
        buffer.append(String.format("\"%s\": {\"value\":\"%s\",\"color\":\"%s\"}", "keyword2", keyword_2_Content, keyword_2_Color));
        buffer.append("}");
        buffer.append("}");
        String params = "";
        try {
            params = new String(buffer.toString().getBytes("UTF-8"));
            System.out.println("utf-8 编码：" + params);
            return params;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getAccessToken() {
        CloseableHttpClient httpCilent = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(WebChatUtil.ACCESS_TOKEN_URL);
        try {
            HttpResponse httpResponse = httpCilent.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String entity = EntityUtils.toString(httpResponse.getEntity());
                AccessToken accessToken = JSONObject.parseObject(entity, AccessToken.class);
                return accessToken.getAccess_token();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放资源
                httpCilent.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}
