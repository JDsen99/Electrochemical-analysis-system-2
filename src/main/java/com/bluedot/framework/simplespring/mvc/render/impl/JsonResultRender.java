package com.bluedot.framework.simplespring.mvc.render.impl;

import com.bluedot.framework.simplespring.mvc.RequestProcessorChain;
import com.bluedot.framework.simplespring.mvc.render.ResultRender;
import com.bluedot.framework.simplespring.util.JsonUtil;
import com.bluedot.framework.simplespring.util.LogUtil;
import com.google.gson.Gson;
import org.slf4j.Logger;

import java.io.PrintWriter;
import java.util.Map;

/**
 * Json渲染器
 *
 * @author xxbb
 */
public class JsonResultRender implements ResultRender {
    private Map<String,Object> jsonData;
    private Logger logger = LogUtil.getLogger();

    public JsonResultRender(Map jsonData) {
        this.jsonData = jsonData;
    }

    @Override
    public void render(RequestProcessorChain requestProcessorChain) throws Exception {
        //设置响应头
        requestProcessorChain.getResp().setContentType("application/json");
        requestProcessorChain.getResp().setCharacterEncoding("UTF-8");
        //响应流写入经过gson格式化之后的处理结果
        try (PrintWriter writer = requestProcessorChain.getResp().getWriter()) {
            writer.write(JsonUtil.toJson(jsonData));
            writer.flush();
        }
        logger.debug("请求响应成功 --- threadName: {}", Thread.currentThread().getName());
    }
}
