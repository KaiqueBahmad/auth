package kaiquebt.dev.auth.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import kaiquebt.dev.auth.config.MailSenderFactory;
import kaiquebt.dev.auth.model.BaseUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService<T extends BaseUser> {
    private final IEmailTemplateBean<T> emailTemplateBean;
    private final JavaMailSender mailSender;

    public EmailService(
        IEmailTemplateBean emailTemplateBean,
        MailSenderFactory mailSenderFactory
    ) {
        this.emailTemplateBean = emailTemplateBean;
        this.mailSender = mailSenderFactory.getMailSender();
    }

    @Value("${kaiquebt.dev.auth.external-url}")
    private String externalUrl;
    
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${kaiquebt.dev.auth.base-path:/api/auth/}")
    private String apiMapping;

    public void sendMagicLink(T user) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(user.getEmail());
            helper.setFrom(fromEmail);
            helper.setSubject(emailTemplateBean.getEmailConfirmTitle());
            
            String magicLinkUrl = UriComponentsBuilder.fromUriString(externalUrl)
                .path(apiMapping + "confirm-email")
                .queryParam("token", user.getEmailConfirmationToken())
                .build()
                .toUriString();

                
            String html = emailTemplateBean.buildEmailConfirm(user, magicLinkUrl);
            
            helper.setText(html, true);
            
            mailSender.send(message);
            log.info("Magic link enviado com sucesso para: {}", user.getEmail()); 
        } catch (MessagingException e) {
            log.error("Erro ao enviar magic link para {}: {}", user.getEmail(), e.getMessage());
            throw new RuntimeException("Erro ao enviar email de confirmação", e);
        }
    }

    public void sendRecoverEmail(T user) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(user.getEmail());
            helper.setFrom(fromEmail);
            helper.setSubject(emailTemplateBean.getRecoverAccountTitle());
                
            String html = emailTemplateBean.buildRecoverAccount(user, user.getPasswordRecoverToken());
            
            helper.setText(html, true);
            
            mailSender.send(message);
            log.info("Email de recuperação enviado com sucesso para: {}", user.getEmail()); 
        } catch (MessagingException e) {
            log.error("Erro ao enviar email de recuperação para {}: {}", user.getEmail(), e.getMessage());
            throw new RuntimeException("Erro ao enviar email de recuperação", e);
        }

    }
    
}
