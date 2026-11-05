package com.guilherme.scheduler.user.model;

import com.guilherme.scheduler.shared.exception.InvalidDataException;
import com.guilherme.scheduler.user.model.enums.Role;
import com.guilherme.scheduler.user.model.valueObjects.Password;
import com.guilherme.scheduler.user.model.valueObjects.PasswordConverter;
import com.guilherme.scheduler.user.model.valueObjects.Phone;
import com.guilherme.scheduler.user.model.valueObjects.PhoneConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.Objects;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    public User(String name, Phone phone, Password password) {
        if(name == null || name.isBlank()) throw new InvalidDataException("Nome nao pode ser vazio");
        if(name.length() > 150) throw new InvalidDataException("Nome nao pode ter mais de 150 caracteres");
        this.name = name;
        this.phone = Objects.requireNonNull(phone, "Telefone nao pode ser nulo/vazio");
        this.role = Role.CLIENT;
        this.password = Objects.requireNonNull(password, "Senha nao pode ser nulo/vazio");;
    }
}
