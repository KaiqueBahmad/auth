package kaiquebt.dev.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConfigurationProperties(prefix = "kaiquebt.dev.auth.mail")
public class CustomMailProperties {
    private String host;
    private Integer port;
    private String username;
    private String password;
    private Boolean sslEnable;
    private Boolean starttlsEnable;
    
    // Método para verificar se está configurado
    public boolean isConfigured() {
        return StringUtils.hasText(host) 
            && port != null 
            && StringUtils.hasText(username) 
            && StringUtils.hasText(password);
    }
    
    // Getters e Setters
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public Boolean getSslEnable() { return sslEnable; }
    public void setSslEnable(Boolean sslEnable) { this.sslEnable = sslEnable; }
    
    public Boolean getStarttlsEnable() { return starttlsEnable; }
    public void setStarttlsEnable(Boolean starttlsEnable) { this.starttlsEnable = starttlsEnable; }
}