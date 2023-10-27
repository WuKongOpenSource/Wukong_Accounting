package com.kakarote.core.config;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.MybatisMapWrapperFactory;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.kakarote.core.common.Const;
import com.kakarote.core.utils.BaseUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * web配置类 以及分页配置
 * @author zhangzhiwei
 */
@Configuration
public class WebConfig {

    @Value("${spring.jackson.timeZone:GMT+8}")
    private String timeZone;

    /**
     * long类型数据统一处理转换为string
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return jacksonObjectMapperBuilder -> {
            jacksonObjectMapperBuilder.serializerByType(Long.TYPE, ToStringSerializer.instance);
            jacksonObjectMapperBuilder.serializerByType(Long.class, ToStringSerializer.instance);
            jacksonObjectMapperBuilder.deserializerByType(Date.class, new DateDeSerializer());
            //localDate类型的序列化
            jacksonObjectMapperBuilder.serializers(new LocalDateTimeSerializer(DatePattern.NORM_DATETIME_FORMATTER), new LocalDateSerializer(DatePattern.NORM_DATE_FORMATTER));
            //localDate类型的反序列化
            jacksonObjectMapperBuilder.deserializers(new LocalDateTimeDeserializer(DatePattern.NORM_DATETIME_FORMATTER), new LocalDateDeserializer(DatePattern.NORM_DATE_FORMATTER));
        };
    }

    /**
     * 对objectMapper增加一些时间类型的处理
     *
     * @return objectMapper
     */
    @Bean
    public ObjectMapper buildObjectMapper() {
        ObjectMapper objectMapper = new JsonMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.setDateFormat(new SimpleDateFormat(DatePattern.NORM_DATETIME_PATTERN));
        objectMapper.setTimeZone(TimeZone.getTimeZone(timeZone));
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DatePattern.NORM_DATETIME_FORMATTER));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DatePattern.NORM_DATE_FORMATTER));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DatePattern.NORM_DATETIME_FORMATTER));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DatePattern.NORM_DATE_FORMATTER));
        javaTimeModule.addDeserializer(Date.class, new DateDeSerializer());
        javaTimeModule.addSerializer(Long.class, ToStringSerializer.instance);
        javaTimeModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(javaTimeModule);
        return objectMapper;
    }


    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(List<InnerInterceptor> interceptors) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        for (InnerInterceptor innerInterceptor : interceptors) {
            interceptor.addInnerInterceptor(innerInterceptor);
        }
        return interceptor;
    }

    @Bean
    public PaginationInnerInterceptor paginationInterceptor() {
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(Const.QUERY_MAX_SIZE * 100);
        paginationInterceptor.setOptimizeJoin(true);
        return paginationInterceptor;
    }

    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return i -> i.setObjectWrapperFactory(new MybatisMapWrapperFactory());
    }

    @Bean
    @Primary
    public IdentifierGenerator idGenerator() {
        return entity -> BaseUtil.getNextId();
    }

    /**
     * 自定义long序列化，因为默认情况下long类型都序列化成string,在需要long类型数据时使用
     * 将注解加到bean属性上即可
     *
     * \@JsonSerialize(using = WebConfig.NumberSerializer.class)
     */
    public static class NumberSerializer extends JsonSerializer<Long> {

        @Override
        public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeNumber(value);
        }
    }

    /**
     * date反序列化
     */
    public static class DateDeSerializer extends JsonDeserializer<Date> {

        @Override
        public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getText();
            if (StrUtil.isNotEmpty(text)) {
                return DateUtil.parse(text);
            }
            return null;
        }
    }
}
