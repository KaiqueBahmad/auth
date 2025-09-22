package kaiquebt.dev.client.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import kaiquebt.dev.auth.model.BaseUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString
@EqualsAndHashCode(callSuper = true)
public class User extends BaseUser {
    
    @Column(name = "other", nullable = true)
    private String other;

    // @Override
    // public String toString() {
    //     return "batata";
    // }

}
