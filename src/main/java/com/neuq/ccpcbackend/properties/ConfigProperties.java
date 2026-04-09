package com.neuq.ccpcbackend.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "ccpc")
@Component
@Data
public class ConfigProperties {
    private String[] nonFilterPath;
    private String[] jwtFilterPath;
    private String[] staticSource;
    public String[] getNonFilterPath() {
        return nonFilterPath;
    }
    public String[] getJwtFilterPath() {
        return jwtFilterPath;
    }
    public String[] getStaticSource() {
        return staticSource;
    }
}
