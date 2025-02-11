package com.prose.service;

import com.prose.entity.Contract;
import com.prose.entity.InternshipOffer;
import com.prose.entity.ProgramManager;
import com.prose.entity.notification.Notification;
import com.prose.entity.notification.NotificationCode;
import com.prose.entity.users.auth.Role;
import com.prose.repository.InternshipOfferRepository;
import com.prose.repository.ProgramManagerRepository;
import com.prose.service.Exceptions.InternshipNotFoundException;
import com.prose.service.Exceptions.InvalidBase64Exception;
import com.prose.service.Exceptions.JobApplicationNotFoundException;
import com.prose.service.Exceptions.MissingPermissionsExceptions;
import com.prose.service.dto.SignatureBase64DTO;
import com.prose.service.dto.UserDTO;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class ContractService {

    private final InternshipOfferRepository internshipOfferRepository;
    private final ProgramManagerRepository programManagerRepository;
    private final NotificationService notificationService;

    public ContractService(InternshipOfferRepository internshipOfferRepository, ProgramManagerRepository programManagerRepository, NotificationService notificationService) {
        this.internshipOfferRepository = internshipOfferRepository;
        this.programManagerRepository = programManagerRepository;
        this.notificationService = notificationService;
    }

    public LocalDateTime sign(UserDTO userApp, long internshipId, SignatureBase64DTO signatureBase64DTO) throws InternshipNotFoundException, MissingPermissionsExceptions, InvalidBase64Exception, IOException, JobApplicationNotFoundException {

        InternshipOffer internshipOffer = internshipOfferRepository.findById(internshipId).orElseThrow(InternshipNotFoundException::new);
        Contract contract = internshipOffer.getContract();
        LocalDateTime now = LocalDateTime.now();

        byte[] signaturePNG;
        // Validation =/
        signaturePNG = Base64.getDecoder().decode(signatureBase64DTO.signature());
        if (ImageIO.read(new ByteArrayInputStream(signaturePNG)) == null) {
            throw new InvalidBase64Exception();
        }

        switch (userApp.role()) {
                case PROGRAM_MANAGER -> {
                    if (contract.getManagerSign() != null) {
                        throw new MissingPermissionsExceptions();
                    }

                    ProgramManager programManager = programManagerRepository.getReferenceById(userApp.id()); //triste
                    contract.setManager(programManager);
                    contract.setManagerSign(now);
                    contract.setManagerSignImage(signaturePNG);
                }
                case EMPLOYEUR -> {
                    if (contract.getEmployerSign() != null || !internshipOffer.getJobOfferApplication().getJobOffer().getEmployeur().getId().equals(userApp.id())) {
                        throw new MissingPermissionsExceptions();
                    }
                    contract.setEmployerSign(now);
                    contract.setEmployerSignImage(signaturePNG);
                }
                case STUDENT -> {
                    if (contract.getStudentSign() != null || !internshipOffer.getJobOfferApplication().getCurriculumVitae().getStudent().getId().equals(userApp.id())) {
                        throw new MissingPermissionsExceptions();
                    }
                    contract.setStudentSign(now);
                    contract.setStudentSignImage(signaturePNG);
                }
                default -> throw new MissingPermissionsExceptions();
            }

        notificationService.tryFindViewNotification(userApp,NotificationCode.CONTRACT_TO_SIGN,internshipOffer.getId());

        if(contract.getStudentSign() != null && contract.getEmployerSign() != null && contract.getManagerSign() != null){
            notificationService.addNotification(Notification.builder()
                    .code(NotificationCode.CONTRACT_SIGNED)
                    .userId(internshipOffer.getEmployeurId())
                    .referenceId(internshipOffer.getId())
                    .session(internshipOffer.getSession())
                    .build());

            notificationService.addNotification(Notification.builder()
                    .code(NotificationCode.CONTRACT_SIGNED)
                    .userId(internshipOffer.getStudentId())
                    .referenceId(internshipOffer.getId())
                    .session(internshipOffer.getSession())
                    .build());

            notificationService.addNotification(Notification.builder()
                    .code(NotificationCode.INTERN_REQUIRE_ASSIGNEMENT)
                    .filter(Role.PROGRAM_MANAGER)
                    .referenceId(internshipOffer.getStudentId())
                    .session(internshipOffer.getSession())
                    .build());
        }

        internshipOfferRepository.save(internshipOffer);
        return now;
    }
}
