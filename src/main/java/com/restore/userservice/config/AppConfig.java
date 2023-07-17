package com.restore.userservice.config;


import com.fasterxml.jackson.databind.JsonNode;
import com.restore.core.config.*;
import com.restore.core.exception.AppExceptionHandler;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Configuration
@EntityScan({"com.restore.core.entity"})
@Import({AppExceptionHandler.class, WebSecurityConfig.class, CORSFilterConfig.class, TenantDataConfig.class, TenantIdentifierResolver.class, FeignConfig.class})
public class AppConfig {


    @Value("${server.port}")
    private String serverPort;


    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public EurekaInstanceConfigBean eurekaInstanceConfig(InetUtils inetUtils) {

        EurekaInstanceConfigBean config = new EurekaInstanceConfigBean(inetUtils);
        config.setNonSecurePort(Integer.parseInt(serverPort));

        try {
            String url = System.getenv("ECS_CONTAINER_METADATA_URI");
            if(url != null) {
                ResponseEntity<JsonNode> response = new RestTemplate().getForEntity(url, JsonNode.class);
                if(response.getStatusCode().is2xxSuccessful()) {
                    String privateIp = response.getBody().withArray("Networks").get(0).withArray("IPv4Addresses").get(0).asText();
                    System.out.println("ECS Private IP : "+privateIp);
                    config.setIpAddress(privateIp);
                    config.setPreferIpAddress(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return config;
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
