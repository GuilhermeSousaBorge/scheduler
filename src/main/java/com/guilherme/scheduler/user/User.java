package com.guilherme.scheduler.user;

import com.guilherme.scheduler.user.valueObjects.Password;
import com.guilherme.scheduler.user.valueObjects.PasswordConverter;
import com.guilherme.scheduler.user.valueObjects.Phone;
import com.guilherme.scheduler.user.valueObjects.PhoneConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(name = "tb_user")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long  id;

    @Column(nullable = false)
    private String name;

    @Convert(converter = PhoneConverter.class)
    @Column(nullable = false, length = 12, unique = true)
    private Phone phone;

    @Column(nullable = false)
    @Convert(converter = PasswordConverter.class)
    private Password password;

    @CreationTimestamp
    @Column(name = "created_at")
    private ZonedDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
}
