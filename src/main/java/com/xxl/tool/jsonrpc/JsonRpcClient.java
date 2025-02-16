package com.xxl.tool.jsonrpc;

import com.xxl.tool.gson.GsonTool;
import com.xxl.tool.net.HttpTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * json-rpc client
 *
 * @author xuxueli
 */
public class JsonRpcClient {
    private static Logger logger = LoggerFactory.getLogger(JsonRpcClient.class);

    private String url;
    private int timeout = 3000;
    private Map<String, String> headers = null;

    public JsonRpcClient(String url) {
        this.url = url;
    }

    public JsonRpcClient(String url, int timeout) {
        this.url = url;
        this.timeout = timeout;
    }

    public JsonRpcClient(String url, int timeout, Map<String, String> headers) {
        this.url = url;
        this.timeout = timeout;
        this.headers = headers;
    }

    public <T> T proxy(Class<T> serviceInterface) {
        return proxy(null, serviceInterface);
    }

    public <T> T proxy(final String service, Class<T> serviceInterface) {
        return (T) Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class[]{serviceInterface},
                (proxy, method, args) -> {
                    String serviceName = service!=null?service:serviceInterface.getName();
                    String methodName = method.getName();
                    return invoke(serviceName, methodName, args, method.getReturnType());
                });

    }

    // 输入输出：object
    public <T> T invoke(String service,
                        String method,
                        Object[] params,
                        Class<T> responseClass) {

        try {
            // params 2 request
            String[] paramJsons = null;
            if (params != null) {
                paramJsons = new String[params.length];
                for (int i = 0; i < params.length; i++) {
                    paramJsons[i] = GsonTool.toJson(params[i]);
                }
            }
            JsonRpcRequest request = new JsonRpcRequest(service, method, paramJsons);

            /**
             * remoting
             *
             * Send：
             *      1、client serilize：Request -> json
             *      2、http remoting
             *      3、server deserilize：json -> Request
             * Receive：
             *      1、server serilize: Response -> json
             *      2、http remoting
             *      3、client deserilize：json -> Response
             *
             */
            String requestJson = GsonTool.toJson(request);
            String responseData = HttpTool.postBody(url, requestJson, timeout, headers);

            if (responseData.isEmpty()) {
                throw new RuntimeException("response data not found");
            }
            JsonRpcResponse response = GsonTool.fromJson(responseData, JsonRpcResponse.class);
            if (response.getError() != null) {
                throw new RuntimeException("response error=" + response.getError());
            }

            // result 2 reponse
            T responseObj = GsonTool.fromJson(response.getData(), responseClass);
            return responseObj;
        } catch (Exception e) {
            logger.debug("client invoke error:{}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
