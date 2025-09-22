package kaiquebt.dev.auth.service;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kaiquebt.dev.auth.dto.SessionHistoryDto;
import kaiquebt.dev.auth.model.BaseUser;
import kaiquebt.dev.auth.model.BaseUserSessionLog;
import kaiquebt.dev.auth.model.UserSessionOrigin;
import kaiquebt.dev.auth.repository.BaseUserRepository;
import kaiquebt.dev.auth.repository.BaseUserSessionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSessionLogService<U extends BaseUser, L extends BaseUserSessionLog<U>> {
    private final BaseUserSessionLogRepository<L> baseUserSessionLogRepository;
    private final RequestContextService requestContextService;
    
    @PersistenceContext
    private EntityManager entityManager;

    public void registerLoginSession(
        U user,
        L log
    ) {
        log.setUser(user);
        log.safeSetSessionOrigin(UserSessionOrigin.LOGIN);
        log.setIpAddress(requestContextService.getClientIp());
        log.setUserAgent(requestContextService.getUserAgent());
        baseUserSessionLogRepository.save(log);
    }

    public void registerRefreshSession(
        U user,
        L log
    ) {
        log.setUser(user);
        log.safeSetSessionOrigin(UserSessionOrigin.REFRESH);
        log.setIpAddress(requestContextService.getClientIp());
        log.setUserAgent(requestContextService.getUserAgent());
        baseUserSessionLogRepository.save(log);
    }

    public void registerImpersonatedSession(
        U user,
        U performedBy,
        L log
    ) {
        log.setUser(user);
        log.setPerformedBy(performedBy);
        log.safeSetSessionOrigin(UserSessionOrigin.IMPERSONATING);
        log.setIpAddress(requestContextService.getClientIp());
        log.setUserAgent(requestContextService.getUserAgent());
        baseUserSessionLogRepository.save(log);
    }

    public void registerImpersonatedRefreshSession(
        U user,
        U performer,
        L log
    ) {
        log.setUser(user);
        log.safeSetSessionOrigin(UserSessionOrigin.REFRESH);
        log.setIpAddress(requestContextService.getClientIp());
        log.setUserAgent(requestContextService.getUserAgent());
        baseUserSessionLogRepository.save(log);
    }

    public Page<SessionHistoryDto> getSessionHistory(
            Long userId,
            Integer page,
            Integer size,
            LocalDateTime startDate,
            LocalDateTime endDate
        ) {
        if (size > 50) {
            size = 50;
        }
        
        PageRequest req = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return this.baseUserSessionLogRepository.findAllProjected(
            req,
            userId,
            startDate,
            endDate
        ).map(SessionHistoryDto::new);
    }


}
