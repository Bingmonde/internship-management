package com.prose.service.dto;

import com.prose.entity.users.Employeur;

public record EmployeurDTO(
        Long id,
        String nomCompagnie,
        String contactPerson,
        String adresse,
        String city,
        String postalCode,
        String telephone,
        String fax,
        String courriel) {


    public static EmployeurDTO toDTO(Employeur employeur) {
        return new EmployeurDTO(
                employeur.getId(),
                employeur.getNomCompagnie(),
                employeur.getContactPerson(),
                employeur.getAdresse(),
                employeur.getCity(),
                employeur.getPostalCode(),
                employeur.getTelephone(),
                employeur.getFax(),
                employeur.getCredentials().getEmail());
    }
}
//                employeur.getPreferredStage(),
//                employeur.getNumberOfInterns(),
//                employeur.getWillingToRehire(),
//                employeur.getComments());


