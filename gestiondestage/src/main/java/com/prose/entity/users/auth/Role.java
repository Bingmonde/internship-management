package com.prose.entity.users.auth;

import lombok.Getter;

public enum Role {
    STUDENT("STUDENT"),
    TEACHER("TEACHER"),
    EMPLOYEUR("EMPLOYEUR"),
    PROGRAM_MANAGER("PROJET_MANAGER");

    private final String string;

    Role(String string){
        this.string = string;
    }

    @Override
    public String toString(){
        return string;
    }

}
