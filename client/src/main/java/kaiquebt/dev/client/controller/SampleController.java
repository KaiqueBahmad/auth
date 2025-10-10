package kaiquebt.dev.client.controller;

import org.springframework.web.bind.annotation.RestController;
import kaiquebt.dev.auth.model.BaseUser.RoleType;
import kaiquebt.dev.auth.service.BaseAuthService;
import kaiquebt.dev.auth.service.BaseAuthService.SignupHook;
import kaiquebt.dev.auth.service.BaseAuthService.SignupRequest;
import kaiquebt.dev.client.model.User;
import kaiquebt.dev.client.model.UserSessionLog;
import kaiquebt.dev.client.repository.UserRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import kaiquebt.dev.auth.config.MailConfiguration;

@RestController
public class SampleController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BaseAuthService<User, UserSessionLog> service;

    private static class HookHandler implements SignupHook<User> {
        private static HookHandler instance = new HookHandler();

        public static HookHandler getInstance() {
            return instance;
        }

        private HookHandler() {

        }

        @Override
        public void afterSave(User user, SignupRequest<User> request) {
            // TODO Auto-generated method stub
            SignupHook.super.afterSave(user, request);
        }

        @Override
        public void afterValidation(User user, SignupRequest<User> request) {
            // TODO Auto-generated method stub
            SignupHook.super.afterValidation(user, request);
        }

        @Override
        public void beforeSave(User user, SignupRequest<User> request) {
            // user.setEmailConfirmationToken(UUID.randomUUID().toString());
            // user.setEmailConfirmed(false);
            // user.setPasswordRecoverTries(1);
        }

        @Override
        public void beforeValidation(User user, SignupRequest<User> request) {
            // TODO Auto-generated method stub
            SignupHook.super.beforeValidation(user, request);
        }

        @Override
        public void onError(Exception error, User user, SignupRequest<User> request) {
            // TODO Auto-generated method stub
            SignupHook.super.onError(error, user, request);
        }
    }

    @GetMapping("/criar")
    public String test() {
        System.out.println("aeiou");

        User user = User.builder()
            .email("kaiquebahmadt@gmail.com")
            .other("aeiou")
            .password("senhaforte")
            .username("kaiquebt")
        .build();

        SignupRequest<User> request = new SignupRequest<User>() {
            @Override
            public User getUser() {
                // TODO Auto-generated method stub
                return user;
            }

            @Override
            public SignupHook<User> getHook() {
                // TODO Auto-generated method stub
                return HookHandler.getInstance();
            }
        };
        service.signup(request);

        return "teste";
    }
    
    @GetMapping("listar")
    public List<User> getMethodName() {
        return this.userRepository.findAll();
    }
    

    // @GetMapping("buscar")
    // public ResponseEntity<?> buscar() {
    // Optional<User> pOpt = productRepository.findByName("nomee");

    // return pOpt.isPresent() ? ResponseEntity.ok(pOpt.get()):
    // ResponseEntity.ok("nada ainda paizao");
    // }

    // @GetMapping("")
    // public String getMethodName() {
    // return this.sampleService.hallo();
    // }

}