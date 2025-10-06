package kaiquebt.dev.auth.config;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class MailConfiguration {
    
    @Bean
    public JavaMailSender customMailSender(
            CustomMailProperties customProps,
            @Lazy JavaMailSender defaultMailSender) {
        
        // Se custom properties estiver configurado, cria sender customizado
        if (customProps.isConfigured()) {
            log.info("✓ Using custom mail configuration: kaiquebt.dev.auth.mail");
            return createCustomMailSender(customProps);
        }
        
        // Senão, retorna o bean padrão do Spring
        log.warn("✓ Fallback to default Spring mail configuration: spring.mail");
        return defaultMailSender;
    }
    
    private JavaMailSender createCustomMailSender(CustomMailProperties props) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        mailSender.setHost(props.getHost());
        mailSender.setPort(props.getPort());
        mailSender.setUsername(props.getUsername());
        mailSender.setPassword(props.getPassword());
        
        Properties javaMailProps = mailSender.getJavaMailProperties();
        javaMailProps.put("mail.transport.protocol", "smtp");
        javaMailProps.put("mail.smtp.auth", "true");
        
        boolean sslEnable = props.getSslEnable() != null ? props.getSslEnable() : false;
        boolean starttlsEnable = props.getStarttlsEnable() != null ? props.getStarttlsEnable() : false;
        
        javaMailProps.put("mail.smtp.ssl.enable", String.valueOf(sslEnable));
        javaMailProps.put("mail.smtp.starttls.enable", String.valueOf(starttlsEnable));
        
        if (sslEnable) {
            javaMailProps.put("mail.smtp.ssl.trust", props.getHost());
        }
        
        return mailSender;
    }

}
