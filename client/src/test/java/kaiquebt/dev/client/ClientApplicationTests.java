package kaiquebt.dev.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

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
import kaiquebt.dev.auth.service.BaseAuthService;
import kaiquebt.dev.auth.service.BaseAuthService.SignupHook;
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

    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean
    private CustomMailSenderWrapper customMailSenderWrapper;

    private MimeMessage mockMimeMessage;

    @BeforeAll
    void setup() {

        mockMimeMessage = new MimeMessage((Session) null);

        when(javaMailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        doAnswer(invocation -> {
            System.out.println("✓ Email enviado com sucesso (mock)");
            return null;
        }).when(javaMailSender).send(any(MimeMessage.class));

        when(customMailSenderWrapper.isPresent()).thenReturn(false);
        when(customMailSenderWrapper.getMailSender()).thenReturn(null);
    }

    @BeforeEach
    void beforeEach() {
        userRepository.deleteAll();

        reset(javaMailSender);

        when(javaMailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void testMailSentOnRegistration() {
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
                return null;
            }
        });

        verify(javaMailSender, atLeastOnce()).send(any(MimeMessage.class));
    }

    @Test
    void tryConfirmRegistration() {
        User user = userRepository.findAll().get(0);
        assertNotNull(user, "Usuário deve existir no banco");

        String token = user.getEmailConfirmation().getToken();
        assertNotNull(token, "Token de confirmação deve existir");

        ResponseEntity<Controller.StandardResponse<ConfirmEmailResponse>> response = controller.confirmEmail(token);

        assertNotNull(response.getBody(), "Response body não deve ser null");
        assertTrue(response.getBody().isSuccess(), "Confirmação deve ser bem sucedida");
        assertNotNull(response.getBody().getData(), "Token JWT deve ser retornado");
        assertNotNull(response.getBody().getData().getToken(), "Token JWT não deve ser null");

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertTrue(updatedUser.getEmailConfirmation().getConfirmed(),
                "Email deve estar confirmado");
    }

    @Test
    void testSignupWithDuplicates() {
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
                return null;
            }
        });

        // check if email duplication throws exception
        assertThrows(
            IllegalArgumentException.class,
            () -> authService.signup(
                    new SignupRequest<User>() {
                        @Override
                        public User getUser() {
                            return User.builder()
                                    .username("kaique2")
                                    .email("kaiq@gmail.com")
                                    .password("123456")
                                    .build();
                        }

                        @Override
                        public BaseAuthService.SignupHook<User> getHook() {
                            return null;
                        }
                })  
        );
        
        // check if we are blocking duplicate usernames
        assertThrows(
            IllegalArgumentException.class,
            () -> authService.signup(
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
                            return null;
                        }
                })  
        );

        // check if we are blocking used email on username
        assertThrows(
            IllegalArgumentException.class,
            () -> authService.signup(
                    new SignupRequest<User>() {
                        @Override
                        public User getUser() {
                            return User.builder()
                                    .username("kaiq@gmail.com")
                                    .email("kaiq2@gmail.com")
                                    .password("123456")
                                    .build();
                        }

                        @Override
                        public BaseAuthService.SignupHook<User> getHook() {
                            return null;
                        }
                })  
        );

    }
    @Test
    void testConfirmEmailWithInvalidToken() {
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
                return null;
            }
        });

        ResponseEntity<Controller.StandardResponse<ConfirmEmailResponse>> response = controller.confirmEmail("invalid-token");
        assertNotNull(response.getBody(), "Response body não deve ser null");
        assertFalse(response.getBody().isSuccess(), "Confirmação deve falhar com token inválido");
        // check if bad request status is returned
        assertEquals(400, response.getStatusCode().value(), "Status code deve ser 400 para token inválido");

    }

    @Test
    void testConfirmEmailWithExpiredToken() {
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
                return null;
            }
        });

        User user = userRepository.findAll().get(0);
        assertNotNull(user, "Usuário deve existir no banco");
        user.getEmailConfirmation().setTokenExpiresAt(LocalDateTime.now().minusHours(1));
        userRepository.save(user);
        String token = user.getEmailConfirmation().getToken();
        assertNotNull(token, "Token de confirmação deve existir");
        ResponseEntity<Controller.StandardResponse<ConfirmEmailResponse>> response = controller.confirmEmail(token);
        assertNotNull(response.getBody(), "Response body não deve ser null");
        assertFalse(response.getBody().isSuccess(), "Confirmação deve falhar com token expirado");
        // check if bad request status is returned
        assertEquals(400, response.getStatusCode().value(), "Status code deve ser 400 para token expirado");
    }

    @Test
    void testConfirmEmailAlreadyConfirmed() {
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
                    return null;
                }
            });

        User user = userRepository.findAll().get(0);
        assertNotNull(user, "Usuário deve existir no banco");
        
        String token = user.getEmailConfirmation().getToken();
        assertNotNull(token, "Token de confirmação deve existir");

        // First confirmation - should succeed
        ResponseEntity<Controller.StandardResponse<ConfirmEmailResponse>> firstResponse = controller.confirmEmail(token);
        assertNotNull(firstResponse.getBody(), "Response body não deve ser null");
        assertTrue(firstResponse.getBody().isSuccess(), "Primeira confirmação deve ser bem sucedida");

        // Second confirmation attempt - should fail
        ResponseEntity<Controller.StandardResponse<ConfirmEmailResponse>> secondResponse = controller.confirmEmail(token);
        assertNotNull(secondResponse.getBody(), "Response body não deve ser null");
        assertFalse(secondResponse.getBody().isSuccess(), "Confirmação deve falhar para email já confirmado");
        assertEquals(400, secondResponse.getStatusCode().value(), "Status code deve ser 400 para email já confirmado");
    }

    @Test
    void testSignupWithInvalidEmail() {
        // Test with completely invalid email format
        assertThrows(
            IllegalArgumentException.class,
            () -> authService.signup(
                    new SignupRequest<User>() {
                        @Override
                        public User getUser() {
                            return User.builder()
                                    .username("kaique")
                                    .email("invalid-email")
                                    .password("123456")
                                    .build();
                        }

                        @Override
                        public BaseAuthService.SignupHook<User> getHook() {
                            return null;
                        }
                    })
        );

        // Test with email missing @ symbol
        assertThrows(
            IllegalArgumentException.class,
            () -> authService.signup(
                    new SignupRequest<User>() {
                        @Override
                        public User getUser() {
                            return User.builder()
                                    .username("kaique2")
                                    .email("invalidemail.com")
                                    .password("123456")
                                    .build();
                        }

                        @Override
                        public BaseAuthService.SignupHook<User> getHook() {
                            return null;
                        }
                    })
        );

        // Test with email missing domain
        assertThrows(
            IllegalArgumentException.class,
            () -> authService.signup(
                    new SignupRequest<User>() {
                        @Override
                        public User getUser() {
                            return User.builder()
                                    .username("kaique3")
                                    .email("invalid@")
                                    .password("123456")
                                    .build();
                        }

                        @Override
                        public BaseAuthService.SignupHook<User> getHook() {
                            return null;
                        }
                    })
        );
    }

    @Test
    void testSignupWithEmptyFields() {
        // Test with empty username
        assertThrows(
            IllegalArgumentException.class,
            () -> authService.signup(
                    new SignupRequest<User>() {
                        @Override
                        public User getUser() {
                            return User.builder()
                                    .username("")
                                    .email("kaiq@gmail.com")
                                    .password("123456")
                                    .build();
                        }

                        @Override
                        public BaseAuthService.SignupHook<User> getHook() {
                            return null;
                        }
                    })
        );

        // Test with empty email
        assertThrows(
            IllegalArgumentException.class,
            () -> authService.signup(
                    new SignupRequest<User>() {
                        @Override
                        public User getUser() {
                            return User.builder()
                                    .username("kaique")
                                    .email("")
                                    .password("123456")
                                    .build();
                        }

                        @Override
                        public BaseAuthService.SignupHook<User> getHook() {
                            return null;
                        }
                    })
        );

        // Test with null username
        assertThrows(
            IllegalArgumentException.class,
            () -> authService.signup(
                    new SignupRequest<User>() {
                        @Override
                        public User getUser() {
                            return User.builder()
                                    .username(null)
                                    .email("kaiq@gmail.com")
                                    .password("123456")
                                    .build();
                        }

                        @Override
                        public BaseAuthService.SignupHook<User> getHook() {
                            return null;
                        }
                    })
        );

        // Test with null email
        assertThrows(
            IllegalArgumentException.class,
            () -> authService.signup(
                    new SignupRequest<User>() {
                        @Override
                        public User getUser() {
                            return User.builder()
                                    .username("kaique")
                                    .email(null)
                                    .password("123456")
                                    .build();
                        }

                        @Override
                        public BaseAuthService.SignupHook<User> getHook() {
                            return null;
                        }
                    })
        );
    }

    @Test
    void testConfirmEmailWithNullToken() {

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
                return null;
            }
        });

        ResponseEntity<Controller.StandardResponse<ConfirmEmailResponse>> response = controller.confirmEmail(null);
        assertNotNull(response.getBody(), "Response body não deve ser null");
        assertFalse(response.getBody().isSuccess(), "Confirmação deve falhar com token null");
        assertEquals(400, response.getStatusCode().value(), "Status code deve ser 400 para token null");
    }

}