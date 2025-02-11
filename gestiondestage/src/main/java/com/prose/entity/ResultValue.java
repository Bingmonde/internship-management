package com.prose.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Setter
@Getter
public class ResultValue<T> {

    private String exception;
    private T value;
}
