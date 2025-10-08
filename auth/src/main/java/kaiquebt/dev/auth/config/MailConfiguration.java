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
    
    public static class CustomMailSenderWrapper {
        private final JavaMailSender mailSender;

        public CustomMailSenderWrapper() {
            this.mailSender = null;
        }

        public CustomMailSenderWrapper(JavaMailSender mailSender) {
            this.mailSender = mailSender;
        }

        public boolean isPresent() {
            return mailSender != null;
        }

        public JavaMailSender getMailSender() {
            return mailSender;
        }
    }

    @Bean
    public CustomMailSenderWrapper customMailSenderWrapper(CustomMailProperties customProps) {        
        if (customProps.isConfigured()) {
            return new CustomMailSenderWrapper(createCustomMailSender(customProps));
        }
        return new CustomMailSenderWrapper();
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
