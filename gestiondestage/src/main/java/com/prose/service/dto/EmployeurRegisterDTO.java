package com.prose.service.dto;

public record EmployeurRegisterDTO(String nomCompagnie,
                                   String courriel,
                                   String contactPerson,
                                   String city,
                                   String postalCode,
                                   String fax,
                                   String telephone,
                                   String adresse,
                                   String mdp) {
}
