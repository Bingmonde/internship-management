package com.prose.security;

import com.prose.entity.users.UserApp;
import com.prose.repository.*;
import com.prose.security.exception.AuthenticationException;
import com.prose.security.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
public class AuthProvider implements AuthenticationProvider{
	private final PasswordEncoder passwordEncoder;
	private final UserAppRepository userAppRepository;

	public AuthProvider(PasswordEncoder passwordEncoder, UserAppRepository userAppRepository) {
        this.passwordEncoder = passwordEncoder;
		this.userAppRepository = userAppRepository;
    }


	@Override
	public Authentication authenticate(Authentication authentication) {
		try {
			UserApp userApp = userAppRepository.findUserAppByEmail(authentication.getPrincipal().toString()).orElseThrow(UserNotFoundException::new);
			validateAuthentication(authentication, userApp.getCredentials().getPassword());
			return new UsernamePasswordAuthenticationToken(
					userApp.getCredentials().getUsername(),
					userApp.getCredentials().getPassword(),
					userApp.getCredentials().getAuthorities()
			);
		} catch (UserNotFoundException e) {
			throw new AuthenticationException(HttpStatus.FORBIDDEN, "Incorrect username or password");
		}
	}

	@Override
	public boolean supports(Class<?> authentication){
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

	private void validateAuthentication(Authentication authentication, String password){
		if(!passwordEncoder.matches(authentication.getCredentials().toString(), password))
			throw new AuthenticationException(HttpStatus.FORBIDDEN, "Incorrect username or password");
	}
}
