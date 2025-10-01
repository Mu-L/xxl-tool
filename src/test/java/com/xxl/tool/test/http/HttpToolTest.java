package com.xxl.tool.test.http;

import com.xxl.tool.gson.GsonTool;
import com.xxl.tool.http.HttpTool;
import com.xxl.tool.http.http.HttpRequest;
import com.xxl.tool.http.http.HttpResponse;
import com.xxl.tool.http.http.enums.ContentType;
import com.xxl.tool.http.http.enums.Method;
import com.xxl.tool.http.http.iface.HttpInterceptor;
import com.xxl.tool.response.Response;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpToolTest {
    private static final Logger logger = LoggerFactory.getLogger(HttpToolTest.class);


    // get 请求
    @Test
    public void test01() {
        HttpResponse httpResponse = HttpTool.createGet("https://news.baidu.com/widget")
                .form("ajax", "json")
                .form("id", "ad")
                .execute();

        logger.info("statusCode: " + httpResponse.statusCode());
        logger.info("response: " + httpResponse.response());
    }

    // post 请求
    @Test
    public void test02() {
        HttpResponse httpResponse = HttpTool.createPost("https://news.baidu.com/widget?ajax=json&id=ad")
                .execute();

        logger.info("statusCode: " + httpResponse.statusCode());
        logger.info("response: " + httpResponse.response());
    }

    // 详细 API 使用
    @Test
    public void test03() {
        HttpRequest httpRequest = HttpTool.createRequest()
                .url("https://news.baidu.com/widget?ajax=json&id=ad")
                .method(Method.GET)
                .contentType(ContentType.JSON)
                .header("header", "value")
                .cookie("cookie", "value")
                .connectTimeout(10000)
                .readTimeout(10000)
                .useCaches(false)
                .body("body")
                .form("form", "value")
                .auth("auth999")
                .interceptor(new HttpInterceptor() {
                    @Override
                    public void before(HttpRequest httpRequest) {
                        logger.info("before, url = " + httpRequest.getUrl());
                    }

                    @Override
                    public void after(HttpRequest httpRequest, HttpResponse httpResponse) {
                        logger.info("after, response = " + httpResponse.response());
                    }
                });

        HttpResponse httpResponse = httpRequest.execute();
        logger.info("statusCode: " + httpResponse.statusCode());
        logger.info("response: " + httpResponse.response());
    }

    // 拦截器
    @Test
    public void test04() {
        RespDTO result = HttpTool.createGet("https://news.baidu.com/widget?ajax=json&id=ad")
                .interceptor(new HttpInterceptor() {
                    @Override
                    public void before(HttpRequest httpRequest) {
                        logger.info("before, url = " + httpRequest.getUrl());
                    }

                    @Override
                    public void after(HttpRequest httpRequest, HttpResponse httpResponse) {
                        logger.info("after, response = " + httpResponse.response());
                    }
                })
                .execute()
                .response(RespDTO.class);
        logger.info("result2: " + result);
    }

    // 设置Cookie，获取返回的 Cookie
    @Test
    public void test05() {
        HttpResponse httpResponse = HttpTool.createGet("https://news.baidu.com/widget?ajax=json&id=ad")
                .cookie("cookie1", "value1")
                .execute();

        logger.info("result2: " + httpResponse);
        logger.info("cookie: " + httpResponse.cookies());
        logger.info("cookie3: " + httpResponse.cookie("cookie3"));
        logger.info("cookie1: " + httpResponse.cookie("cookie1"));
    }

    // 获取返回内容，直接返回 string
    @Test
    public void test06() {
        String result = HttpTool.createPost("https://news.baidu.com/widget?ajax=json&id=ad")
                .execute()
                .response();

        logger.info("result2: " + result);
    }

    // 获取返回内容，序列化为 对象
    @Test
    public void test07() {
        RespDTO result = HttpTool.createGet("https://news.baidu.com/widget?ajax=json&id=ad")
                .execute()
                .response(RespDTO.class);

        logger.info("result2: " + result);
    }

    // RequestBody + ResponseBody； 输入对象 + 返回对象，底层自动处理json处理
    @Test
    public void test08() {
        RespDTO result = HttpTool.createPost("https://news.baidu.com/widget?ajax=json&id=ad")
                .request(new RespDTO("jack", 18))
                .execute()
                .response(RespDTO.class);

        logger.info("result2: " + result);
    }

    // RequestBody + ResponseBody： 模拟数据
    @Test
    public void test09() {
        String json = GsonTool.toJson(Response.ofSuccess(new RespDTO("jack", 18)));

        // 模拟返回数据
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatusCode(200);
        httpResponse.setResponse(json);

        // 解析返回数据
        Response<RespDTO> result = httpResponse.response(Response.class, RespDTO.class);
        Response<RespDTO> result2 = httpResponse.response(Response.class);

        logger.info("statusCode: " + httpResponse.statusCode());
        logger.info("response: " + httpResponse.response());
        logger.info("result: " + result);
        logger.info("result2: " + result2);
    }


    // ------------------------ model ------------------------------------

    public static class RespDTO {
        private String request_id;
        private long timestamp;

        public RespDTO() {
        }

        public RespDTO(String request_id, long timestamp) {
            this.request_id = request_id;
            this.timestamp = timestamp;
        }

        public String getRequest_id() {
            return request_id;
        }

        public void setRequest_id(String request_id) {
            this.request_id = request_id;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return "Resp{" +
                    "request_id='" + request_id + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }

}