package kaiquebt.dev.auth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import kaiquebt.dev.auth.service.IPasswordValidator;

@Configuration
public class Config {
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }    

    //bean for password validator when missing
    @Bean
    @ConditionalOnMissingBean(IPasswordValidator.class)
    public IPasswordValidator passwordValidator() {
        return new IPasswordValidator() {
            @Override
            public void doValidate(String password) throws IllegalArgumentException {
            }
        };
    }
}
