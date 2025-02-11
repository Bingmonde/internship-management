package com.prose.gestiondestage.security;

import com.prose.entity.users.Student;
import com.prose.repository.*;
import com.prose.security.AuthProvider;
import com.prose.security.exception.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthProviderTests {


    UserAppRepository userAppRepository;
    AuthProvider authProvider;

    @BeforeEach
    void beforeEach() {
        userAppRepository = mock(UserAppRepository.class);
        authProvider = new AuthProvider(NoOpPasswordEncoder.getInstance(), userAppRepository);
    }

    @Test
    void authenticateTest() {
        // Arrange
        String email = "hi";
        String password = "mdp";

        Student student = new Student();
        student.setId(0L);
        student.setCredentials(email,password);
        String primary = email;

        Authentication authentication = new UsernamePasswordAuthenticationToken(primary,password);
        when(userAppRepository.findUserAppByEmail(anyString())).thenReturn(Optional.of(student));
        // Act
        Authentication returnAuthentication = authProvider.authenticate(authentication);
        // Assert
        assertThat(returnAuthentication.getCredentials()).isEqualTo(authentication.getCredentials());
        assertThat(returnAuthentication.getPrincipal()).isEqualTo(authentication.getPrincipal());
        assertThat(returnAuthentication.getAuthorities().toString()).isEqualTo(student.getAuthorities().toString());
    }

    @Test
    void authenticateNoMatch() {
        // Arrange
        String email = "hi";
        String password = "mdp";

        Student student = new Student();
        student.setId(0L);
        student.setCredentials(email,"password");
        String primary = email;

        Authentication authentication = new UsernamePasswordAuthenticationToken(primary,password);
        when(userAppRepository.findUserAppByEmail(anyString())).thenReturn(Optional.of(student));
        // Act
        assertThatThrownBy(() -> authProvider.authenticate(authentication))
                .isInstanceOf(AuthenticationException.class);// Assert

    }

    @Test
    void authenticateTestNotFound() {
        // Arrange
        String email = "hi";
        String password = "mdp";
        String primary = email;

        Authentication authentication = new UsernamePasswordAuthenticationToken(primary,password);
        when(userAppRepository.findUserAppByEmail(anyString())).thenReturn(Optional.empty());
        // Act
        assertThatThrownBy(() -> authProvider.authenticate(authentication))
                .isInstanceOf(AuthenticationException.class); // Assert
    }

    @Test
    void supportsTest() {
        // Act
        boolean support = authProvider.supports(UsernamePasswordAuthenticationToken.class);
        // Assert
        assertThat(support).isTrue();
    }

    // Coverage
    @Test
    void supportsNotTest() {
        // Act
        boolean support = authProvider.supports(RememberMeAuthenticationToken.class);
        // Assert
        assertThat(support).isFalse();
    }
}
