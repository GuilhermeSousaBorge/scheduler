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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long  id;

    @Column(nullable = false)
    private String name;

    @Convert(converter = PhoneConverter.class)
    @Column(nullable = false, length = 14, unique = true)
    private Phone phone;

    @Column(nullable = false)
    @Convert(converter = PasswordConverter.class)
    private Password password;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    public User(String name, Phone phone, Password password) {
        if(name == null || name.isBlank() || name.length() > 150) throw new IllegalArgumentException("Invalid name supplied");
        this.name = name;
        this.phone = phone;
        this.password = password;
    }
}
