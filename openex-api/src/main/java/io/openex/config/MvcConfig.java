package io.openex.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;
import java.util.List;

@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

  private final static int CACHE_PERIOD = 3600;

  @Resource
  private ObjectMapper objectMapper;

  @Bean
  public MappingJackson2HttpMessageConverter customJackson2HttpMessageConverter() {
    MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
    jsonConverter.setObjectMapper(this.objectMapper);
    return jsonConverter;
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
    //https://springdoc.org/#why-am-i-getting-an-error-swagger-ui-unable-to-render-definition-when-overriding-the-default-spring-registered-httpmessageconverter
    messageConverters.add(new ByteArrayHttpMessageConverter());
    messageConverters.add(new StringHttpMessageConverter());
    messageConverters.add(customJackson2HttpMessageConverter());
  }

  private void addPathStaticResolver(ResourceHandlerRegistry registry, String pattern, String location) {
    registry
        .addResourceHandler(pattern)
        .addResourceLocations(location)
        .setCachePeriod(CACHE_PERIOD)
        .resourceChain(false)
        .addResolver(new EncodedResourceResolver())
        .addResolver(new PathResourceResolver());
  }

  @Override
  public void addResourceHandlers(@NotNull ResourceHandlerRegistry registry) {
    // React statics
    addPathStaticResolver(registry, "/static/**", "classpath:/build/static/");
    // Specific application images
    addPathStaticResolver(registry, "/media/**", "classpath:/build/static/media/");
  }
}
