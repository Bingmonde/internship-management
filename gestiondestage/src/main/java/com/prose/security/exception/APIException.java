package com.prose.security.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public abstract class APIException extends RuntimeException{
	@Getter
	protected final HttpStatus status;
	protected final String message;

	public APIException(HttpStatus status, String message){
		this.status = status;
		this.message = message;
		System.out.println("APIException constructor");
	}

	@Override
	public String getMessage(){
		return message;
	}
}
