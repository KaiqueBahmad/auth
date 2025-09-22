package kaiquebt.dev.client.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import kaiquebt.dev.auth.model.BaseUserSessionLog;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@ToString
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_session_logs")
public class UserSessionLog extends BaseUserSessionLog<User> {
    
}
