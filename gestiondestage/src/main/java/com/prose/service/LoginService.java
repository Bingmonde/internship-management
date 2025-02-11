package com.prose.service;

import com.prose.entity.ProgramManager;
import com.prose.entity.users.UserApp;
import com.prose.repository.*;
import com.prose.security.AuthProvider;
import com.prose.security.JwtTokenProvider;
import com.prose.security.exception.AuthenticationException;
import com.prose.security.exception.UserNotFoundException;
import com.prose.service.Exceptions.InvalidPasswordException;
import com.prose.service.dto.UserDTO;
import com.prose.service.dto.auth.JWTAuthResponse;
import com.prose.service.dto.auth.LoginDTO;
import com.prose.service.dto.notifications.NotificationRootDTO;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class LoginService {

    private final AuthProvider authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserAppRepository userAppRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginService(AuthProvider authenticationManager, JwtTokenProvider jwtTokenProvider, UserAppRepository userAppRepository, StudentService studentService, EmployeurService employeurService, TeacherService teacherService, ProgramManagerService programManagerService, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userAppRepository = userAppRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public JWTAuthResponse authenticateUser(LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getEmail(),loginDTO.getPassword()));
        return new JWTAuthResponse(jwtTokenProvider.generateToken(authentication), authentication.getAuthorities().toArray()[0].toString());
    }

    public UserDTO getUserByEmail(String email) {
        return UserDTO.toDTO(userAppRepository.findUserAppByEmail(email).orElseThrow(UserNotFoundException::new));
    }

    public UserDTO getUserByEmailValidatePassword(String email, String password) throws InvalidPasswordException {
        UserApp userApp = userAppRepository.findUserAppByEmail(email).orElseThrow(UserNotFoundException::new);
        if (!passwordEncoder.matches(password, userApp.getPassword())) {
            throw new InvalidPasswordException("Password is incorrect");
        }
        return UserDTO.toDTO(userApp);
    }

}
