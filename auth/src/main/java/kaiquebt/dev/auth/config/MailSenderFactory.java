package kaiquebt.dev.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import kaiquebt.dev.auth.config.MailConfiguration.CustomMailSenderWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MailSenderFactory {
    @Autowired
    private JavaMailSender defaultMailSender;

    @Autowired
    private CustomMailSenderWrapper customMailSenderWrapper;

    private static boolean first = false;

    public JavaMailSender getMailSender() {
        boolean hasCustomMailSender = customMailSenderWrapper != null && customMailSenderWrapper.getMailSender() != null;

        if (!first) {
            if (hasCustomMailSender) {
                log.info("✓ Using custom mail configuration: kaiquebt.dev.auth.mail");
            } else {
                log.info("✓ Using default Spring mail configuration: spring.mail");
            }
            first = true;
        }

        if (hasCustomMailSender) {
            return customMailSenderWrapper.getMailSender();
        }
        return defaultMailSender;

    }

}
