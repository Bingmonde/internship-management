package com.prose.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    //TODO : Autre façon de signer?
    private LocalDateTime employerSign;
    private byte[] employerSignImage;
    private LocalDateTime studentSign;
    private byte[] studentSignImage;
    private LocalDateTime managerSign;
    private byte[] managerSignImage;

    @ManyToOne
    private ProgramManager manager;


}
