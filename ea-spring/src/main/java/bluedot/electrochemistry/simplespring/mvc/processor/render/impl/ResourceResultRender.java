package bluedot.electrochemistry.simplespring.mvc.processor.render.impl;

import bluedot.electrochemistry.simplespring.mvc.RequestProcessorChain;
import bluedot.electrochemistry.simplespring.mvc.processor.render.ResultRender;

/**
 * @author Senn
 * @create 2022/1/26 19:43
 */
public class ResourceResultRender implements ResultRender {

    @Override
    public void render(RequestProcessorChain requestProcessorChain) throws Exception {
        //TODO do nothing here
        requestProcessorChain.getRequest().setCharacterEncoding("GBK");
    }
}
