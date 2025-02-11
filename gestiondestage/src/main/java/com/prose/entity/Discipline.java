package com.prose.entity;

import com.prose.service.dto.DisciplineTranslationDTO;

import java.util.ArrayList;
import java.util.List;

public enum Discipline {
    INFORMATIQUE("informatique","Computer Science","Informatique"),
    TECH_INFERMIERE("tech_infermiere", "Nursing","Soins infirmiers"),
    ARCHITECTURE("architechture","Architect","Architecte"),
    CIVIL_ENGINEER("civil_engineer","Civil engineering","Génie civil"),
    ACCOUNTING("accounting","Accounting and Management","Comptabilité et gestion"),
    MARKETING("marketing","Marketing","Commercialisation"),
    SOCIAL_WORKER("social_worker","Social Worker","Travailleur(euse) social"),
    EC_EDUCATION("ec_education","Early Childhood Education","Éducation de la petite enfance");

    private final String val;
    private final String english; //Hardcoded pour l'instant. Si on avait un traducteur professionnel, l'avoir dans un fichier serait meilleur...
    private final String french;

    private Discipline(String val, String english, String french) {
        this.val = val;
        this.english = english;
        this.french = french;
    }

    public DisciplineTranslationDTO getTranslation() {
        return new DisciplineTranslationDTO(val,english,french);
    }

    public String toString() {
        return val;
    }

    public static Discipline toEnum(String val) {
        for (Discipline v : values()) {
            if (v.toString().equalsIgnoreCase(val)) {
                return v;
            }
        }
        throw new IllegalArgumentException("Invalid discipline value: " + val);
    }


    // Bien que dans notre cas, le coût d'envoyer toutes les traductions est minime, il serait techniquement possible, dans le futur, de vouloir envoyer une seule langue.
    // Mais ce n'est pas planifié pour l'instant
    public static List<DisciplineTranslationDTO> getAllTranslations() {
        List<DisciplineTranslationDTO> trans = new ArrayList<>();
        for (Discipline v : values()) {
            trans.add(v.getTranslation());
        }
        return trans;
    }

    public static boolean isValidDiscipline(String discipline) {
        for (Discipline v : values()) {
            if (v.toString().equalsIgnoreCase(discipline)) {
                return true;
            }
        }
        return false;
    }
}
