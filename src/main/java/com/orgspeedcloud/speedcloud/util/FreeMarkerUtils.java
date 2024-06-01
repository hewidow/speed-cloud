package com.orgspeedcloud.speedcloud.util;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.StringWriter;
import java.util.Map;


/**
 * FreeMarker模板工具类
 * @author Chen
 */
@Component
public class FreeMarkerUtils {
    @Resource
    Configuration configuration;

    public String makeTemplate(String fileName, Map model){
        try{
            StringWriter stringWriter = new StringWriter();
            Template template = configuration.getTemplate(fileName);
            template.process(model, stringWriter);
            stringWriter.flush();
            return stringWriter.toString();
        }catch (Exception ex){
            System.out.println(ex);
        }
        return null;
    }
}
