package com.example.demo.Entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "CERTIFICATION_VALUES")
@Getter
@Setter
public class CertificationValues {

    @Id
    @Column
    private String certificationValue;

    @Column
    private Date exporedDt;

}
