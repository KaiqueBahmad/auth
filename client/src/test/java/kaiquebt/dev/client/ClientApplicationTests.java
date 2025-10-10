package kaiquebt.dev.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Set;

import kaiquebt.dev.auth.ConfirmEmailResponse;
import kaiquebt.dev.auth.Controller;
import kaiquebt.dev.auth.config.MailConfiguration.CustomMailSenderWrapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import kaiquebt.dev.auth.model.BaseUser.RoleType;
import kaiquebt.dev.auth.service.BaseAuthService;
import kaiquebt.dev.auth.service.BaseAuthService.SignupRequest;
import kaiquebt.dev.client.model.User;
import kaiquebt.dev.client.model.UserSessionLog;
import kaiquebt.dev.client.repository.UserRepository;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.mail.host=localhost",
        "spring.mail.port=3025",
        "spring.mail.username=test@test.com",
        "spring.mail.password=test",
        "kaiquebt.dev.auth.jwt-secret=daf66e01593f61a15b857cf433aae03a005812b31234e149036bcc8dee755dbb",
        "kaiquebt.dev.auth.jwt-expiration-milliseconds=3600000",
        "kaiquebt.dev.auth.external-url=http://localhost:8080"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClientApplicationTests {

    @Autowired
    private Controller controller;

    @Autowired
    private BaseAuthService<User, UserSessionLog> authService;

    @Autowired
    private UserRepository userRepository;

    // Mock apenas o JavaMailSender, não o EmailService nem o Factory
    @MockitoBean
    private JavaMailSender javaMailSender;

    // Mock o CustomMailSenderWrapper para retornar null
    // Isso força o MailSenderFactory a usar o defaultMailSender (que é nosso mock)
    @MockitoBean
    private CustomMailSenderWrapper customMailSenderWrapper;

    private MimeMessage mockMimeMessage;

    @BeforeAll
    void setup() {
        // Cria um MimeMessage mock que será reutilizado
        mockMimeMessage = new MimeMessage((Session) null);

        // Configura o javaMailSender mock
        when(javaMailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        doAnswer(invocation -> {
            System.out.println("✓ Email enviado com sucesso (mock)");
            return null;
        }).when(javaMailSender).send(any(MimeMessage.class));

        // Configura o wrapper para retornar null
        // Isso faz com que o MailSenderFactory use o defaultMailSender (nosso mock)
        when(customMailSenderWrapper.isPresent()).thenReturn(false);
        when(customMailSenderWrapper.getMailSender()).thenReturn(null);
    }

    @BeforeEach
    void beforeEach() {
        // Limpa o banco de dados antes de cada teste
        userRepository.deleteAll();

        // Reseta o mock para contar as chamadas corretamente
        reset(javaMailSender);

        // Reconfigura o mock após o reset
        when(javaMailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        doNothing().when(javaMailSender).send(any(MimeMessage.class));

        // Cria um usuário de teste
        authService.signup(
                new SignupRequest<User>() {
                    @Override
                    public User getUser() {
                        return User.builder()
                                .username("kaique")
                                .email("kaiq@gmail.com")
                                .password("123456")
                                .build();
                    }

                    @Override
                    public BaseAuthService.SignupHook<User> getHook() {
                        return null; // Não precisa de hook, getRoles() já define as roles
                    }
                }
        );
        System.out.println("TOKEN: "+ userRepository.findAll().get(0).getEmailConfirmation().getToken());
    }

    @Test
    void testMailSentOnRegistration() {
        // Verifica que o email foi enviado durante o signup no @BeforeEach
        verify(javaMailSender, atLeastOnce()).send(any(MimeMessage.class));
    }

    @Test
    void tryConfirmRegistration() {
        User user = userRepository.findAll().get(0);
        assertNotNull(user, "Usuário deve existir no banco");

        String token = user.getEmailConfirmation().getToken();
        assertNotNull(token, "Token de confirmação deve existir");

        ResponseEntity<Controller.StandardResponse<ConfirmEmailResponse>> response =
                controller.confirmEmail(token);

        assertNotNull(response.getBody(), "Response body não deve ser null");
        assertTrue(response.getBody().isSuccess(), "Confirmação deve ser bem sucedida");
        assertNotNull(response.getBody().getData(), "Token JWT deve ser retornado");
        assertNotNull(response.getBody().getData().getToken(), "Token JWT não deve ser null");

        // Verifica que o email foi confirmado no banco
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertTrue(updatedUser.getEmailConfirmation().getConfirmed(),
                "Email deve estar confirmado");
    }

    @Test
    void testMockIsWorking() {
        // Verifica que o mock está funcionando
        assertNotNull(javaMailSender, "JavaMailSender mock não deve ser null");

        MimeMessage message = javaMailSender.createMimeMessage();
        assertNotNull(message, "MimeMessage criado não deve ser null");

        // Testa o envio
        javaMailSender.send(message);
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testMultipleUserRegistration() {
        // Testa registro de múltiplos usuários
        int initialEmailCount = mockingDetails(javaMailSender)
                .getInvocations()
                .size();

        authService.signup(
                new SignupRequest<User>() {
                    @Override
                    public User getUser() {
                        return User.builder()
                                .username("user2")
                                .email("user2@gmail.com")
                                .password("password2")
                                .build();
                    }

                    @Override
                    public BaseAuthService.SignupHook<User> getHook() {
                        return null;
                    }
                }
        );

        // Deve ter enviado mais um email
        verify(javaMailSender, atLeast(initialEmailCount + 1))
                .send(any(MimeMessage.class));
    }
}