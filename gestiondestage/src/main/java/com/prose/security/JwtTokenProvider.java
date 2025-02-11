package com.prose.security;

import com.prose.security.exception.InvalidJwtTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider{
	@Value("${application.security.jwt.expiration}")
	private int expirationInMs;
	@Value("${application.security.jwt.secret-key}")
	private final String jwtSecret = "2A508E3CEDA7CB46C84F96FAFC1709B734C438ED95B3AC7B8BAB9BAD04633AB8";

	public String generateToken(Authentication authentication){
		long nowMillis = System.currentTimeMillis();
		JwtBuilder builder = Jwts.builder()
			.setSubject(authentication.getName())
			.setIssuedAt(new Date(nowMillis))
			.setExpiration(new Date(nowMillis + expirationInMs))
			.claim("authorities", authentication.getAuthorities())
			.signWith(key());
		return builder.compact();
	}

	private Key key(){
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
	}

	public String getJwtUsername(String token){
		return Jwts.parser()
			.setSigningKey(key())
			.build()
			.parseClaimsJws(token)
			.getBody()
			.getSubject();
	}

	public void validateToken(String token){
		try{
			Jwts.parser().setSigningKey(key()).build().parseClaimsJws(token);
		}catch(SecurityException ex){
			throw new InvalidJwtTokenException(HttpStatus.BAD_REQUEST, "Invalid JWT signature");
		}catch(MalformedJwtException ex){
			throw new InvalidJwtTokenException(HttpStatus.BAD_REQUEST, "Invalid JWT token");
		}catch(ExpiredJwtException ex){
			throw new InvalidJwtTokenException(HttpStatus.BAD_REQUEST, "Expired JWT token");
		}catch(UnsupportedJwtException ex){
			throw new InvalidJwtTokenException(HttpStatus.BAD_REQUEST, "Unsupported JWT token");
		}catch(IllegalArgumentException ex){
			throw new InvalidJwtTokenException(HttpStatus.BAD_REQUEST, "JWT claims string is empty");
		}
	}

}
