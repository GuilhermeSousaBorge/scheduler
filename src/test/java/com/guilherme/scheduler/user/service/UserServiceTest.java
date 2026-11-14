package com.guilherme.scheduler.user.service;

import com.guilherme.scheduler.shared.config.CurrentUser;
import com.guilherme.scheduler.user.exception.CannotChangeMyOwnRoleException;
import com.guilherme.scheduler.user.exception.UserNotFoundException;
import com.guilherme.scheduler.user.dto.UserResponseDTO;
import com.guilherme.scheduler.user.mapper.UserMapper;
import com.guilherme.scheduler.user.model.User;
import com.guilherme.scheduler.user.model.enums.Role;
import com.guilherme.scheduler.user.model.valueObjects.Password;
import com.guilherme.scheduler.user.model.valueObjects.Phone;
import com.guilherme.scheduler.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    private UserMapper userMapper = new UserMapper();

    @InjectMocks
    private UserService userService;

    private static final Long OWNER_ID = 1L;
    private static final Long OTHER_ID = 2L;
    private static final Long ATTENDANT_ID = 3L;
    private static final Long ADMIN_ID = 4L;

    @Test
    void clienteQueNaoEDonoRecebeNotFoundSemConsultarOBanco() {
        var outroCliente = new CurrentUser(OTHER_ID, Role.CLIENT);

        assertThrows(UserNotFoundException.class,
                () -> userService.findUserById(OWNER_ID, outroCliente));

        verifyNoInteractions(userRepository);
    }

    @Test
    void clienteQueEDonoConsegueVerOProprioPerfil(){
        var clienteDono = new CurrentUser(OWNER_ID, Role.CLIENT);

        User user = new User("cliente", new Phone("+5534911111111"), Password.fromHash("123456"));

        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.findUserById(OWNER_ID, clienteDono);

        assertEquals("cliente", response.name());
        assertEquals(new Phone("+5534911111111"), new Phone(response.phone()));
    }

    @Test
    void atendenteConsegueVerOPerfilDeOutro(){
        var atendente = new CurrentUser(ATTENDANT_ID, Role.ATTENDANT);

        User user = new User("cliente", new Phone("+5534911111111"), Password.fromHash("123456"));

        when(userRepository.findById(OTHER_ID)).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.findUserById(OTHER_ID, atendente);

        assertEquals("cliente", response.name());
        assertEquals(new Phone("+5534911111111"), new Phone(response.phone()));
    }

    @Test
    void adminConsegueVerOPerfilDeOutro(){
        var admin = new CurrentUser(ADMIN_ID, Role.ADMIN);

        User user = new User("cliente", new Phone("+5534911111111"), Password.fromHash("123456"));

        when(userRepository.findById(OTHER_ID)).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.findUserById(OTHER_ID, admin);

        assertEquals("cliente", response.name());
        assertEquals(new Phone("+5534911111111"), new Phone(response.phone()));
    }

    @Test
    void idInexistenteRecebeExcecaoDoMesmoCasoDoNaoEDono(){
        var admin = new CurrentUser(ADMIN_ID, Role.ADMIN);
        var outroCliente = new CurrentUser(OTHER_ID, Role.CLIENT);



        User user = new User("cliente", new Phone("+5534911111111"), Password.fromHash("123456"));

        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        var excecaoIdNaoExiste = assertThrows(UserNotFoundException.class,
                () ->  userService.findUserById(5L, admin));

        var excecaoClienteNaoEDono = assertThrows(UserNotFoundException.class,
                () -> userService.findUserById(OWNER_ID, outroCliente));

        assertEquals(excecaoClienteNaoEDono.getMessage(), excecaoIdNaoExiste.getMessage());
    }

    @Test
    void adminNaoConsegueMudarOProprioCargo(){
        var admin = new CurrentUser(ADMIN_ID, Role.ADMIN);

        assertThrows(CannotChangeMyOwnRoleException.class,
                () -> userService.changeRole(ADMIN_ID, Role.ATTENDANT, admin));
        verifyNoInteractions(userRepository);
    }

    @Test
    void adminMudandoOCargoDeUmIdInexistente(){
        var admin = new CurrentUser(ADMIN_ID, Role.ADMIN);

        when(userRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class,
                () -> userService.changeRole(10L, Role.ATTENDANT, admin));
        verify(userRepository).findById(10L);
    }

    @Test
    void adminMudandoOCargoDeUmIdExistente(){
        var admin = new CurrentUser(ADMIN_ID, Role.ADMIN);

        User user = new User(
                "cliente", new Phone("+5534911111111"), Password.fromHash("123456")
        );
        when(userRepository.findById(OTHER_ID)).thenReturn(Optional.of(user));

        userService.changeRole(OTHER_ID, Role.ATTENDANT, admin);

        assertEquals(Role.ATTENDANT, user.getRole());
    }
}
