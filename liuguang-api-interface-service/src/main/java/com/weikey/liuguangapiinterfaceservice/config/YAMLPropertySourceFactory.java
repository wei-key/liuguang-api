package com.weikey.liuguangapiinterfaceservice.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import java.io.IOException;
import java.util.Properties;
 
public class YAMLPropertySourceFactory implements PropertySourceFactory {
 
    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource encodedResource) throws IOException {
        //1. 创建一个YAML文件的解析工厂。
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        //2. 设置资源。
        factory.setResources(encodedResource.getResource());
 
        //3. 获取解析后的Properties对象
        Properties properties = factory.getObject();
        //4. 返回：此时不能像默认工厂那样返回ResourcePropertySource对象 ，要返回他的父类PropertiesPropertySource对象
        return name != null ? new PropertiesPropertySource(name, properties) :
                new PropertiesPropertySource(encodedResource.getResource().getFilename(),properties);
    }
}
