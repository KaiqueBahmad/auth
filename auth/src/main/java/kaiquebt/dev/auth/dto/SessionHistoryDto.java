package kaiquebt.dev.auth.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import kaiquebt.dev.auth.model.UserSessionOrigin;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SessionHistoryDto {
    private Long id;
    private UserSessionOrigin origin;
    private String ipAddress;
    private String userAgent;
    private String performedBy;
    private Long createdAt;

    public SessionHistoryDto(ISessionHistoryDto iSession) {
        this.id = iSession.getId();
        this.origin = iSession.getOrigin();
        this.ipAddress = iSession.getIpAddress();
        this.userAgent = iSession.getUserAgent();
        this.performedBy = iSession.getPerformedBy();

        this.createdAt = iSession.getCreatedAt() != null ? 
            iSession.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null;
    }
    

    public static interface ISessionHistoryDto {       
        Long getId();
        UserSessionOrigin getOrigin();
        String getIpAddress();
        String getUserAgent();
        String getPerformedBy();
        LocalDateTime getCreatedAt();
    }
}
