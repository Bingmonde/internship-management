package com.prose.presentation;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prose.entity.*;
import com.prose.entity.users.auth.Role;
import com.prose.security.JwtTokenProvider;
import com.prose.security.exception.UserNotFoundException;
import com.prose.service.*;
import com.prose.service.Exceptions.*;
import com.prose.service.dto.*;
import com.prose.service.dto.auth.JWTAuthResponse;
import com.prose.service.dto.auth.LoginDTO;
import com.prose.service.dto.notifications.NotificationRootDTO;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;


@RestController
public class Controller {
    private static final Logger logger = LoggerFactory.getLogger(JobOfferService.class);

    private final StudentService studentService;
    private final EmployeurService employeurService;
    private final TeacherService teacherService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final LoginService loginService;
    private final ProgramManagerService programManagerService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JobOfferService jobOfferService;
    private final NotificationService notificationService;
    private final PDFService pdfService;
    private final CurriculumVitaeService curriculumVitaeService;
    private final ContractService contractService;
    private final EvaluationEmployerService evaluationEmployerService;


    public Controller(StudentService studentService,
                      TeacherService teacherService,
                      EmployeurService employeurService, EmployeurService employeurService1, TeacherService teacherService1, LoginService loginService, ProgramManagerService programManagerService, JwtTokenProvider jwtTokenProvider, JobOfferService jobOfferService, CurriculumVitaeService curriculumVitaeService,
                      NotificationService notificationService, ContractService contractService, PDFService pdfService, EvaluationEmployerService evaluationEmployerService) {
        this.studentService = studentService;
        this.employeurService = employeurService;
        this.teacherService = teacherService;
        this.loginService = loginService;
        this.programManagerService = programManagerService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jobOfferService = jobOfferService;
        this.curriculumVitaeService = curriculumVitaeService;
        this.notificationService = notificationService;
        this.contractService = contractService;
        this.pdfService = pdfService;
        this.evaluationEmployerService = evaluationEmployerService;
    }

    @PostMapping("/inscription/students")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<ResultValue<StudentDTO>> inscriptionStudents(@RequestBody StudentRegisterDTO studentRegisterDTO){
        ResultValue<StudentDTO> resultValue = new ResultValue<>();
        try{
            StudentDTO studentDTO1 = studentService.createStudent(studentRegisterDTO);
            resultValue.setValue(studentDTO1);
            return ResponseEntity.ok(resultValue);
        } catch (InvalidUserFormatException e) {
            resultValue.setException("InvalidUserFormat");
            return ResponseEntity.badRequest().body(resultValue);
        } catch (AlreadyExistsException e) {
            resultValue.setException("AlreadyExists");
            return ResponseEntity.unprocessableEntity().body(resultValue);
        }
    }

    @PostMapping("/inscription/employeurs")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<ResultValue<EmployeurDTO>> inscriptionEmployeurs(@RequestBody EmployeurRegisterDTO employeurRegisterDTO){
        ResultValue<EmployeurDTO> resultValue = new ResultValue<>();
        try{
            EmployeurDTO employeurDTO = employeurService.createEmployeur(employeurRegisterDTO);
            resultValue.setValue(employeurDTO);
            return ResponseEntity.ok(resultValue);
        } catch (InvalidUserFormatException e) {
            resultValue.setException("InvalidUserFormat");
            return ResponseEntity.badRequest().body(resultValue);
        } catch (AlreadyExistsException e) {
            resultValue.setException("AlreadyExists");
            return ResponseEntity.unprocessableEntity().body(resultValue);
        }
    }

    @PostMapping("/inscription/teachers")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<ResultValue<TeacherDTO>> inscriptionTeacher(@RequestBody TeacherRegisterDTO teacherRegisterDTO){
        ResultValue<TeacherDTO> resultValue = new ResultValue<>();
        try{
            TeacherDTO teacherDTO = teacherService.createTeacher(teacherRegisterDTO);
            resultValue.setValue(teacherDTO);
            return ResponseEntity.ok(resultValue);
        } catch (InvalidUserFormatException e) {
            resultValue.setException("InvalidUserFormat");
            return ResponseEntity.badRequest().body(resultValue);
        } catch (AlreadyExistsException e) {
            resultValue.setException("AlreadyExists");
            return ResponseEntity.unprocessableEntity().body(resultValue);
        }
    }

    @PostMapping("/inscription/programManagers")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<ResultValue<ProgramManagerDTO>> inscriptionTeacher(@RequestBody ProgramManagerRegisterDTO programManagerRegisterDTO){
        ResultValue<ProgramManagerDTO> resultValue = new ResultValue<>();
        try{
            ProgramManagerDTO programManagerDTO = programManagerService.createProgramManager(programManagerRegisterDTO);
            resultValue.setValue(programManagerDTO);
            return ResponseEntity.ok(resultValue);
        } catch (InvalidUserFormatException e) {
            resultValue.setException("InvalidUserFormat");
            return ResponseEntity.badRequest().body(resultValue);
        } catch (AlreadyExistsException e) {
            resultValue.setException("AlreadyExists");
            return ResponseEntity.unprocessableEntity().body(resultValue);
        }
    }

    @PostMapping("/connect")
    public ResponseEntity<ResultValue<JWTAuthResponse>> connectUser(@RequestBody LoginDTO loginDTO) {
        ResultValue<JWTAuthResponse> resultValue = new ResultValue<>();
        try {
            JWTAuthResponse jwtAuthResponse = loginService.authenticateUser(loginDTO);
            if(jwtAuthResponse.getRole().equalsIgnoreCase(Role.STUDENT.toString())){
                studentService.setLastConnection(loginService.getUserByEmail(loginDTO.getEmail()).id());
            }
            resultValue.setValue(loginService.authenticateUser(loginDTO));
            return ResponseEntity.accepted()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(resultValue);
        } catch (Exception e) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
    }

    @GetMapping("/userinfo/username")
    public ResponseEntity<ResultValue<String>> getUser(HttpServletRequest request) {
        ResultValue<String> resultValue = new ResultValue<>();
        String token = request.getHeader("Authorization");
        String email = jwtTokenProvider.getJwtUsername(token);
        if (email == null) {
            resultValue.setException("JWT");
            return ResponseEntity.badRequest().body(resultValue);
        }
        try {
            UserDTO user = loginService.getUserByEmail(email);
            switch (user.role()) {
                case STUDENT -> {
                    StudentDTO studentDTO = studentService.getStudent(user.id());
                    resultValue.setValue(studentDTO.prenom() + " " + studentDTO.nom());
                }
                case TEACHER -> {
                    TeacherDTO teacherDTO = teacherService.getTeacher(user.id());
                    resultValue.setValue(teacherDTO.prenom() + " " + teacherDTO.nom());
                }
                case EMPLOYEUR -> {
                    EmployeurDTO employeurDTO = employeurService.getEmployeur(user.id());
                    resultValue.setValue(employeurDTO.nomCompagnie());
                }
                case PROGRAM_MANAGER -> {
                    ProgramManagerDTO programManagerDTO = programManagerService.getProgramManager(user.id());
                    resultValue.setValue(programManagerDTO.prenom() + " " + programManagerDTO.nom());
                }
            }
        } catch (UserNotFoundException e) {
            resultValue.setException("NotFound");
            return ResponseEntity.status(401).body(resultValue);
        }
        return ResponseEntity.ok(resultValue);
    }

    @GetMapping("/userinfo/myProfile")
    public ResponseEntity<ResultValue<ProfileDTO>> getUserProfile(HttpServletRequest request) {
        ResultValue<ProfileDTO> resultValue = new ResultValue<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        try {
            UserDTO user = loginService.getUserByEmail(jwtUsername);
            switch (user.role()) {
                case STUDENT -> {
                    StudentDTO studentDTO = studentService.getStudent(user.id());
                    ProfileDTO profile = new ProfileDTO(user.role(), studentDTO);
                    resultValue.setValue(profile);
                }
                case TEACHER -> {
                    TeacherDTO teacherDTO = teacherService.getTeacher(user.id());
                    ProfileDTO profile = new ProfileDTO(user.role(), teacherDTO);
                    resultValue.setValue(profile);
                }
                case EMPLOYEUR -> {
                    EmployeurDTO employeurDTO = employeurService.getEmployeur(user.id());
                    ProfileDTO profile = new ProfileDTO(user.role(), employeurDTO);
                    resultValue.setValue(profile);
                }
                case PROGRAM_MANAGER -> {
                    ProgramManagerDTO programManagerDTO = programManagerService.getProgramManager(user.id());
                    ProfileDTO profile = new ProfileDTO(user.role(), programManagerDTO);
                    resultValue.setValue(profile);
                }
            }
        } catch (UserNotFoundException e) {
            resultValue.setException("NotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (RoleAndUserTypeNotCompatibleException e) {
            resultValue.setException("RoleAndUserTypeNotCompatible");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultValue);
        }
        return ResponseEntity.ok(resultValue);
    }


    @GetMapping("/disciplines")
    @CrossOrigin(origins = "http://localhost:3000")
    public List<DisciplineTranslationDTO> sendDisciplineTranslations() {
        return Discipline.getAllTranslations();
    }


    @PostMapping("/employeur/jobOffers")
    public ResponseEntity<ResultValue<JobOfferDTO>> createJobOffer(@RequestPart("jobOffre") JobOfferRegisterDTO jobOfferRegisterDTO,
                                                                   @RequestPart("file")@Nullable MultipartFile uploadFile, HttpServletRequest request) {
        ResultValue<JobOfferDTO> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        Role role = loginService.getUserByEmail(jwtUsername).role();

        if (role != Role.EMPLOYEUR) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try {
            JobOfferDTO jobOfferDTO = jobOfferService.createJobOffer(jobOfferRegisterDTO, uploadFile, jwtUsername);

            resultValue.setValue(jobOfferDTO);
            return ResponseEntity.ok(resultValue);
        } catch (IOException e){
            resultValue.setException("ReadingPDFFails");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultValue);
        }

    }
    @PutMapping("/userinfo/teacher/{id}")
    public ResponseEntity<TeacherDTO> updateUserProfile(@PathVariable Long id, @RequestBody TeacherDTO updateDTO) {
        try {
            TeacherDTO updatedTeacher = teacherService.updateTeacher(id, updateDTO);
            return ResponseEntity.ok(updatedTeacher);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (InvalidPasswordException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
    @PutMapping("/userinfo/student/{id}")
    public ResponseEntity<StudentDTO> updateUserProfile(@PathVariable Long id, @RequestBody StudentDTO updateDTO) {
        try {
            StudentDTO updatedStudent = studentService.updateStudent(id, updateDTO);
            return ResponseEntity.ok(updatedStudent);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    @PutMapping("/userinfo/employeur/{id}")
    public ResponseEntity<EmployeurDTO> updateEmployerProfile(@PathVariable Long id, @RequestBody EmployeurDTO updateDTO) {
        try {
            EmployeurDTO updatedEmployer = employeurService.updateEmployeur(id, updateDTO);
            return ResponseEntity.ok(updatedEmployer);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    @PutMapping("/userinfo/projet_manager/{id}")
    public ResponseEntity<ProgramManagerDTO> updateUserProfile(@PathVariable Long id, @RequestBody ProgramManagerDTO updateDTO) {
        try {
            ProgramManagerDTO updatedProgramManager = programManagerService.updateProgramManager(id, updateDTO);
            return ResponseEntity.ok(updatedProgramManager);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


    @PutMapping("userinfo/teacher/password/{id}")
    public ResponseEntity<TeacherDTO> updateUserPassword(@PathVariable Long id, @RequestBody UpdatePasswordDTO updatePasswordDTO) {
        try {
            TeacherDTO updatedTeacher = teacherService.updateTeacherPassword(id, updatePasswordDTO);
            return ResponseEntity.ok(updatedTeacher);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (InvalidPasswordException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
    @PutMapping("userinfo/student/password/{id}")
    public ResponseEntity<StudentDTO> updateStudentPassword(@PathVariable Long id, @RequestBody UpdatePasswordDTO updatePasswordDTO) {
        try {
            StudentDTO updatedStudent = studentService.updateStudentPassword(id, updatePasswordDTO);
            return ResponseEntity.ok(updatedStudent);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (InvalidPasswordException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
    @PutMapping("userinfo/employeur/password/{id}")
    public ResponseEntity<EmployeurDTO> updateEmployerPassword(@PathVariable Long id, @RequestBody UpdatePasswordDTO updatePasswordDTO) {
        try {
            EmployeurDTO updatedEmployer = employeurService.updateEmployerPassword(id, updatePasswordDTO);
            return ResponseEntity.ok(updatedEmployer);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (InvalidPasswordException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
    @PutMapping("/userinfo/projet_manager/password/{id}")
    public ResponseEntity<ProgramManagerDTO> updateProgramManagerPassword(@PathVariable Long id, @RequestBody UpdatePasswordDTO updatePasswordDTO) {
        try {
            ProgramManagerDTO updatedProgramManager = programManagerService.updateProgramManagerPassword(id, updatePasswordDTO);
            return ResponseEntity.ok(updatedProgramManager);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (InvalidPasswordException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @GetMapping("/employeur/jobOffers")
    public ResponseEntity<ResultValue<List<JobOfferDTO>>> getJobOffersByEmployeur(
            @RequestParam(name = "season") String season,
            @RequestParam(name = "year") String year,
            HttpServletRequest request) {
        ResultValue<List<JobOfferDTO>> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        Role role = loginService.getUserByEmail(jwtUsername).role();

        if (role != Role.EMPLOYEUR) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        EmployeurDTO employeurDTO;
        try {
            employeurDTO = employeurService.getEmployeurByEmail(jwtUsername);
        } catch (UserNotFoundException e) {
            resultValue.setException("EmployeurNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
        try {
            SessionDTO sessionDTO = jobOfferService.getSessionFromDB(season, year);
            List<JobOfferDTO> jobOffers = jobOfferService.getJobOffersByEmployeur(employeurDTO.id(), sessionDTO.id());
            resultValue.setValue(jobOffers);
        } catch (SessionNotFoundException e) {
            resultValue.setException("SessionNotFoundException");
        }
        return ResponseEntity.ok(resultValue);
    }

    private InternshipOfferDTO getInternshipOfferDTOOrNullFromJobOfferApplicationId(long jobOfferApplicationId) {
        try {
            return jobOfferService.getInternshipOfferFromJobOfferApplication(jobOfferApplicationId);
        } catch (InternshipNotFoundException e) {
            return null;
        }
    }

    @GetMapping("/employeur/jobApplications/{id}")
    public ResponseEntity<ResultValue<JobOfferApplicationFullDTO>> getJobOfferApplicationById(@PathVariable Long id, HttpServletRequest request) {
        ResultValue<JobOfferApplicationFullDTO> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        Role role = loginService.getUserByEmail(jwtUsername).role();

        if (role != Role.EMPLOYEUR) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try {
            JobOfferApplicationDTO jobOfferApplicationDTO = jobOfferService.getJobOfferApplication(id);

            resultValue.setValue(new JobOfferApplicationFullDTO(jobOfferApplicationDTO, jobOfferService.getInterviewFromJobOfferApplication(jobOfferApplicationDTO),getInternshipOfferDTOOrNullFromJobOfferApplicationId(jobOfferApplicationDTO.id())));
        }catch (JobApplicationNotFoundException e) {
            resultValue.setException("Not found");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        return ResponseEntity.ok(resultValue);
    }


    @GetMapping("/employeur/jobOffers/listeJobApplications/{offerId}")
    public ResponseEntity<PageResultValueList<JobOfferApplicationFullDTO>> getListJobApplicationByOfferId(
            @RequestParam(name = "season") String season,
            @RequestParam(name = "year") String year,
            @RequestParam("page") int pageNumber,
            @RequestParam("size") int pageSize,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "candidatesStatusFilter", required = false) String candidatesStatusFilterStr,
            @PathVariable Long offerId,
            HttpServletRequest request) {
        PageResultValueList<JobOfferApplicationFullDTO> resultValue = new PageResultValueList<>();

        if (query == null) {
            query = "";
        }

        // turn candidatesStatusFilter into an enum value
        CandidatesStatusFilterForJobApplicationsFull candidatesStatusFilter;
        try {
            if (candidatesStatusFilterStr != null && !candidatesStatusFilterStr.isEmpty()) {
                candidatesStatusFilter = CandidatesStatusFilterForJobApplicationsFull.toEnum(candidatesStatusFilterStr);
            } else {
                candidatesStatusFilter = CandidatesStatusFilterForJobApplicationsFull.ALL;
            }
        } catch (IllegalArgumentException e) {
            resultValue.setException("Invalid candidatesStatusFilter value: " + candidatesStatusFilterStr);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultValue);
        }

        System.out.println(candidatesStatusFilter);

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        Role role = loginService.getUserByEmail(jwtUsername).role();

        if (role != Role.EMPLOYEUR) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try {
            SessionDTO sessionDTO = jobOfferService.getSessionFromDB(season, year);
            Page<JobOfferApplicationDTO> jobOfferApplicationDTOS = jobOfferService.getJobOfferApplicationsFromJobOfferId(offerId, sessionDTO.id(),query,candidatesStatusFilter,pageNumber,pageSize);

            List<JobOfferApplicationFullDTO> jobOfferApplicationFullDTOList = jobOfferApplicationDTOS.toList().stream()
                    .map((jobOfferApplicationDTO) ->new JobOfferApplicationFullDTO(jobOfferApplicationDTO,jobOfferService.getInterviewFromJobOfferApplication(jobOfferApplicationDTO),getInternshipOfferDTOOrNullFromJobOfferApplicationId(jobOfferApplicationDTO.id())))
                    .toList();

            resultValue.setValue(jobOfferApplicationFullDTOList,jobOfferApplicationDTOS.getNumber(),jobOfferApplicationDTOS.getSize(),jobOfferApplicationDTOS.getTotalPages());
        }catch (JobNotFoundException e) {
            resultValue.setException("No valid applications found for job offer with ID " + offerId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }catch (SessionNotFoundException e) {
            resultValue.setException("SessionNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }

       return ResponseEntity.ok(resultValue);
    }

    @PostMapping("/employeur/jobInterview")
    public ResponseEntity<ResultValue<JobInterviewDTO>> createJobInterView(@RequestBody JobInterviewRegisterDTO jobInterviewRegisterDTO, HttpServletRequest request) {
        ResultValue<JobInterviewDTO> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        UserDTO userDTO = loginService.getUserByEmail(jwtUsername);
        Role role = userDTO.role();

        if (role != Role.EMPLOYEUR) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        return getResultValueResponseEntity(jobInterviewRegisterDTO, userDTO, resultValue);
    }

    @GetMapping("/jobInterview/{id}")
    public ResponseEntity<ResultValue<JobInterviewDTO>> getJobInterview(@PathVariable(name = "id") long id, HttpServletRequest request) {
        ResultValue<JobInterviewDTO> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        UserDTO userDTO = loginService.getUserByEmail(jwtUsername);
        Role role = userDTO.role();

        if (role != Role.EMPLOYEUR && role != Role.STUDENT) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try {
            resultValue.setValue(jobOfferService.getInterviewFromId(id));
            return ResponseEntity.ok(resultValue);
        } catch (InterviewNotFoundException e) {
            resultValue.setException("NotFound");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
    }

    @GetMapping("/internship/{id}")
    public ResponseEntity<ResultValue<InternshipOfferDTO>> getInternship(@PathVariable(name = "id") long id, HttpServletRequest request) {
        ResultValue<InternshipOfferDTO> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        UserDTO userDTO = loginService.getUserByEmail(jwtUsername);
        Role role = userDTO.role();

        if (role != Role.EMPLOYEUR && role != Role.STUDENT) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try {
            resultValue.setValue(jobOfferService.getInternshipOffer(id));
            return ResponseEntity.ok(resultValue);
        } catch (InternshipNotFoundException e) {
            resultValue.setException("NotFound");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
    }

    @GetMapping("/employeur/jobInterview")
    public ResponseEntity<PageResultValueList<JobInterviewDTO>> getEmployerInterview(
            @RequestParam(name = "season") String season,
            @RequestParam(name = "year") String year,
            @RequestParam("page") int pageNumber,
            @RequestParam("size") int pageSize,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestParam(name = "status", required = false) String status,
            HttpServletRequest request) {
        PageResultValueList<JobInterviewDTO> resultValue = new PageResultValueList<>();

        if (startDate == null) {
            startDate = "";
        }

        if (endDate == null) {
            endDate = "";
        }

        if (status == null) {
            status = "";
        }

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        UserDTO userDTO = loginService.getUserByEmail(jwtUsername);
        Role role = userDTO.role();

        if (role != Role.EMPLOYEUR) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try {
            resultValue.setValue(employeurService.getJobInterview(userDTO.id(), jobOfferService.getSessionFromDB(season,year).id(),startDate,endDate,status,pageNumber,pageSize));
        } catch (SessionNotFoundException e) {
            resultValue.setException("SessionNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (InvalidUserFormatException e) {
            resultValue.setException("InvalidFormat");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultValue);
        }

        return ResponseEntity.ok(resultValue);
    }

    @DeleteMapping("/employeur/jobInterview/{id}")
    public ResponseEntity<ResultValue<JobInterviewDTO>> cancelInterviewEmployer(@PathVariable Long id, HttpServletRequest request) {
        ResultValue<JobInterviewDTO> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        UserDTO userDTO = loginService.getUserByEmail(jwtUsername);
        Role role = userDTO.role();

        if (role != Role.EMPLOYEUR) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try {
            resultValue.setValue(employeurService.cancelInterview(id,userDTO.id()));
        } catch (InterviewNotFoundException e) {
            resultValue.setException("NotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (MissingPermissionsExceptions e) {
            resultValue.setException("MissingPermission");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        } catch (AlreadyExistsException e) {
            resultValue.setException("AlreadyCancelled");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        return ResponseEntity.ok(resultValue);
    }

    @PutMapping("/employeur/jobOffers/{id}")
    public ResponseEntity<ResultValue<JobOfferDTO>> updateJobOffer(@PathVariable Long id, @RequestBody JobOfferRegisterDTO jobOfferRegisterDTO, HttpServletRequest request) {
        ResultValue<JobOfferDTO> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        Role role = loginService.getUserByEmail(jwtUsername).role();

        if (role != Role.EMPLOYEUR) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try {
            JobOfferDTO updatedJobOffer = jobOfferService.updateJobOffer(id, jobOfferRegisterDTO, jwtUsername);
            resultValue.setValue(updatedJobOffer);
            return ResponseEntity.ok(resultValue);
        } catch (UserNotFoundException e) {
            resultValue.setException("JobOfferNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (MissingPermissionsExceptions e) {
            resultValue.setException("MissingPermissions");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
    }

    @PutMapping("/employeur/offerInternshipToStudent")
    public ResponseEntity<ResultValue<InternshipOfferDTO>> offerInternShipToStudent(
            @RequestParam(name = "jobApplicationId") Long jobApplicationId,
            @RequestParam(name = "offerExpireInDays") Long offerExpireIn,
            HttpServletRequest request) {

        ResultValue<InternshipOfferDTO> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        UserDTO userDTO = loginService.getUserByEmail(jwtUsername);

        if (userDTO.role() != Role.EMPLOYEUR) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try{
            InternshipOfferDTO internshipOfferDTO = jobOfferService.offerInternshipToStudent(
                    jobApplicationId, userDTO, offerExpireIn);
            resultValue.setValue(internshipOfferDTO);
            return ResponseEntity.ok(resultValue);
        } catch (JobNotFoundException e) {
            resultValue.setException("JobOfferNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (UserNotFoundException e) {
            resultValue.setException("UserNotFoundException");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (MaxInternsReachedException e) {
            resultValue.setException("MaxInternsReached");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultValue);
        } catch (MissingPermissionsExceptions | JacksonException e) {
            resultValue.setException("MissingPermissions");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        } catch (NotEnoughTimeAllocatedException e) {
            resultValue.setException("NotEnoughTimeAllocated");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultValue);
        }
    }


    // EQ6-21
    @GetMapping("/student/internshipOffers")
    public ResponseEntity<PageResultValueList<InternshipOfferDTO>> getMyInternshipOffers(
            @RequestParam(name = "season") String season,
            @RequestParam(name = "year") String year,
            @RequestParam(name = "page") int pageNumber,
            @RequestParam(name = "size") int pageSize,
            @RequestParam(name = "q", required = false) String query,
            HttpServletRequest request){
        PageResultValueList<InternshipOfferDTO> resultValue = new PageResultValueList<>();

        if (query == null) {
            query = "";
        }

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        UserDTO userDTO = loginService.getUserByEmail(jwtUsername);

        if (userDTO.role() != Role.STUDENT) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try {
            SessionDTO sessionDTO = jobOfferService.getSessionFromDB(season, year);
            Page<InternshipOfferDTO> internshipOffers = jobOfferService.getMyInternshipOffers(userDTO.id(), sessionDTO.id(), query, pageNumber, pageSize);
            resultValue.setValue(internshipOffers);
            return ResponseEntity.ok(resultValue);
        } catch (InternshipNotFoundException e){
            resultValue.setException("NoInternshipOffersFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (SessionNotFoundException e) {
            resultValue.setException("SessionNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }

    // EQ6-21
    @PutMapping("/student/internshipOffers/{id}/confirmation")
    public ResponseEntity<ResultValue<InternshipOfferDTO>> confirmIntershipOffer(@PathVariable Long id, @RequestParam String status, HttpServletRequest request){
        ResultValue<InternshipOfferDTO> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        UserDTO userDTO = loginService.getUserByEmail(jwtUsername);

        if (userDTO.role() != Role.STUDENT) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try {
            InternshipOfferDTO internshipOfferDTO = jobOfferService.confirmInternshipOffer(id, status ,userDTO.id());
            resultValue.setValue(internshipOfferDTO);
            return ResponseEntity.ok(resultValue);
        } catch (InternshipNotFoundException e){
            resultValue.setException("NoInternshipOffersFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (MissingPermissionsExceptions e) {
            resultValue.setException("InternshipOfferNotYours");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        } catch (DateExpiredException e) {
            resultValue.setException("DateExpired");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultValue);
        } catch (InternshipOfferAlreadyConfirmedException e) {
            resultValue.setException("InternshipOfferAlreadyConfirmed");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultValue);
        } catch (IllegalArgumentException e){
            resultValue.setException("InvalidApprovalStatus");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultValue);
        } catch (JobNotFoundException e) {
            resultValue.setException("DatabaseError");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultValue);
        }
    }


    // PUT utilise POST methode pour evider des codes répétitives

    @PostMapping("/internshipManager/validateInternship/{id}")
    public ResponseEntity<ResultValue<JobPermissionDTO>> createJobPermission(@PathVariable Long id,
                                                                             @RequestBody JobPermissionRegisterDTO jobPermissionRegisterDTO, HttpServletRequest request) {
        ResultValue<JobPermissionDTO> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        UserDTO userDTO = loginService.getUserByEmail(jwtUsername);
        Role role = userDTO.role();

        if (role != Role.PROGRAM_MANAGER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try {
            JobPermissionDTO jobPermissiondb = programManagerService.createJobPermission(id, jobPermissionRegisterDTO,userDTO);
            resultValue.setValue(jobPermissiondb);
            return ResponseEntity.ok(resultValue);
        } catch (JobNotFoundException e) {
            resultValue.setException("JobOfferNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (DisciplineNotFoundException e) {
            resultValue.setException("DisciplineNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (UserNotFoundException e) {
            resultValue.setException("StudentListContainsInvalidUser");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (NullPointerException e) {
            resultValue.setException("JobPermissionFormatIsNull");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultValue);
        }
    }

    @GetMapping("/jobOffers/waitingForApproval")
    public ResponseEntity<ResultValue<List<JobOfferDTO>>> getJobOffersWaitingForApproval(
            HttpServletRequest request,
            @RequestParam(name = "season") String season,
            @RequestParam(name = "year") String year) {
        ResultValue<List<JobOfferDTO>> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        Role role = loginService.getUserByEmail(jwtUsername).role();

        SessionDTO sessionDTO = jobOfferService.getSessionFromDB(season, year);
        if (role != Role.PROGRAM_MANAGER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try {
            List<JobOfferDTO> jobOffers = programManagerService.getJobOffersWaitingForApproval(sessionDTO.id());
            resultValue.setValue(jobOffers);
            return ResponseEntity.ok(resultValue);
        } catch (JobNotFoundException e) {
            resultValue.setException("NoJobOffersWaitingForApproval");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (SessionNotFoundException e){
            resultValue.setException("SessionNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }

    @GetMapping("/students/disciplines/{discipline}")
    public ResponseEntity<PageResultValueList<StudentDTO>> getStudentsByDiscipline(@PathVariable String discipline,
                                                                                   @RequestParam(name = "page") int pageNumber,
                                                                                   @RequestParam(name = "size") int pageSize,
                                                                                   @RequestParam(name = "q", required = false) String query,
                                                                                   HttpServletRequest request) {
        PageResultValueList<StudentDTO> resultValue = new PageResultValueList<>();

        if (query == null) {
            query = "";
        }

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        Role role = loginService.getUserByEmail(jwtUsername).role();

        if (role != Role.PROGRAM_MANAGER && role != Role.TEACHER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try {
            Page<StudentDTO> students = studentService.getStudentsByDiscipline(discipline,query,pageNumber,pageSize);
            resultValue.setValue(students);
            return ResponseEntity.ok(resultValue);
        } catch (DisciplineNotFoundException e) {
            resultValue.setException("DisciplineNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (UserNotFoundException e) {
            resultValue.setException("NoStudentsFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }

    @GetMapping("/student/jobInterview")
    public ResponseEntity<PageResultValueList<JobInterviewDTO>> getListJobInterview(
            @RequestParam(name = "season") String season,
            @RequestParam(name = "year") String year,
            @RequestParam("page") int pageNumber,
            @RequestParam("size") int pageSize,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestParam(name = "status", required = false) String status,
            HttpServletRequest request) throws JobNotFoundException {
        PageResultValueList<JobInterviewDTO> resultValue = new PageResultValueList<>();

        if (startDate == null) {
            startDate = "";
        }

        if (endDate == null) {
            endDate = "";
        }

        if (status == null) {
            status = "";
        }

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        UserDTO userDTO = loginService.getUserByEmail(jwtUsername);
        Role role = userDTO.role();

        if (role != Role.STUDENT) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try{
            SessionDTO sessionDTO = jobOfferService.getSessionFromDB(season, year);
            Page<JobInterviewDTO> jobInterviews = studentService.getJobInterviews(userDTO.id(), sessionDTO.id(),startDate,endDate,status,pageNumber,pageSize);
            resultValue.setValue(jobInterviews);
        }catch (JobNotFoundException e) {
            resultValue.setException("JobNotFoundExeption");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }catch (SessionNotFoundException e){
            resultValue.setException("SessionNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (InvalidUserFormatException e) {
            resultValue.setException("InvalidFormat");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultValue);
        }
        return ResponseEntity.ok(resultValue);
    }

    @PostMapping("/student/jobInterview/confirmation/{jobInterviewId}")
    public ResponseEntity<ResultValue<JobInterviewDTO>> confirmJobInterview(@PathVariable Long jobInterviewId, HttpServletRequest request) {
        ResultValue<JobInterviewDTO> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        UserDTO userDTO = loginService.getUserByEmail(jwtUsername);
        Role role = userDTO.role();

        if (role != Role.STUDENT) {
            resultValue.setException("Forbidden");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try{
            resultValue.setValue(jobOfferService.approveJobInterview(jobInterviewId,userDTO));
        } catch (JobNotFoundException e) {
            resultValue.setException("JobNotFoundException");
        } catch (MissingPermissionsExceptions e) {
            resultValue.setException("MissingPermissions");
        }
        return ResponseEntity.ok(resultValue);
    }

    @GetMapping("jobOffers/{id}")
    public ResponseEntity<ResultValue<JobOfferDTO>> getJobOfferFromId(@PathVariable(name = "id") Long id,HttpServletRequest request) {
        ResultValue<JobOfferDTO> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        Role role = loginService.getUserByEmail(jwtUsername).role();

        if (role != Role.STUDENT && role != Role.PROGRAM_MANAGER) {
            // pas un étudiant, non autorisé
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try {
            resultValue.setValue(jobOfferService.getJobOfferDTOById(id));
            return ResponseEntity.ok(resultValue);
        } catch (JobNotFoundException e) {
            resultValue.setException("NotFound");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
    }

    @GetMapping("student/jobOffers")
    public ResponseEntity<PageResultValueList<JobOfferDTO>> getJobOffersForConnectedStudent(
            @RequestParam(name = "season") String season,
            @RequestParam(name = "year") String year,
            @RequestParam(name = "page") int pageNumber,
            @RequestParam(name = "size") int pageSize,
            @RequestParam(name = "q", required = false) String query,
            HttpServletRequest request) {
        PageResultValueList<JobOfferDTO> resultValue = new PageResultValueList<>();

        if (query == null) {
            query = "";
        }

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        Role role = loginService.getUserByEmail(jwtUsername).role();

        if (role != Role.STUDENT) {
            // pas un étudiant, non autorisé
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        StudentDTO studentDTO;
        try {
            SessionDTO sessionDTO = jobOfferService.getSessionFromDB(season, year);
            studentDTO = studentService.getStudentByEmail(jwtUsername);
            Page<JobOfferDTO> jobOffers = jobOfferService.getJobOffersForStudent(studentDTO.courriel(), sessionDTO.id(), query, pageNumber, pageSize);
            resultValue.setValue(jobOffers);
        } catch (UserNotFoundException e) {
            resultValue.setException("StudentNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (MissingPermissionsExceptions e) {
            resultValue.setException("MissingPermissions");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        } catch (SessionNotFoundException e) {
            resultValue.setException("SessionNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }


        return ResponseEntity.ok(resultValue);
    }

    @PostMapping("/student/jobOffers/apply")
    public ResponseEntity<ResultValue<JobOfferApplicationDTO>> applyToJobOffer(@RequestBody JobOfferApplicationRegisterDTO applicationRegisterDTO,HttpServletRequest request) {
        ResultValue<JobOfferApplicationDTO> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        UserDTO userDTO = loginService.getUserByEmail(jwtUsername);
        Role role = userDTO.role();

        if (role != Role.STUDENT) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try {
            resultValue.setValue(jobOfferService.applyToJobOffer(applicationRegisterDTO.jobOffer(), applicationRegisterDTO.cv(), userDTO));
            return ResponseEntity.ok(resultValue);
        } catch (CVNotFoundException | JobNotFoundException e) {
            resultValue.setException("NotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
        catch (MissingPermissionsExceptions e) {
            resultValue.setException("MissingPermission");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        } catch (TooManyApplicationsExceptions e) {
            resultValue.setException("AlreadyApplied");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
    }

    @GetMapping("/student/applications")
    public ResponseEntity<ResultValue<List<JobOfferApplicationDTO>>> getUserApplications(
            @RequestParam(name = "season") String season,
            @RequestParam(name = "year") String year,
            HttpServletRequest request) {
        ResultValue<List<JobOfferApplicationDTO>> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        UserDTO userDTO = loginService.getUserByEmail(jwtUsername);
        Role role = userDTO.role();

        if (role != Role.STUDENT) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try{
            resultValue.setValue(jobOfferService.getJobOffersApplicationsFromStudent(userDTO.id(), jobOfferService.getSessionFromDB(season, year).id()));
        } catch (SessionNotFoundException e) {
            resultValue.setException("SessionNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }

        return ResponseEntity.ok(resultValue);
    }

    @PostMapping("/student/applications/cancel/{id}")
    public ResponseEntity<ResultValue<String>> cancelJobOfferApplication(@PathVariable("id") String id,HttpServletRequest request) {
        ResultValue<String> resultValue = new ResultValue<>();

        long applicationId = 0L; // Juste pour le scope.
        try {
             applicationId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            resultValue.setException("NotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        UserDTO userDTO = loginService.getUserByEmail(jwtUsername);
        Role role = userDTO.role();

        if (role != Role.STUDENT) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try {
            jobOfferService.cancelJobOfferApplicationStudent(applicationId,userDTO.id());
            resultValue.setValue("ok");
            return ResponseEntity.ok(resultValue);
        } catch (JobNotFoundException e) {
            resultValue.setException("NotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (MissingPermissionsExceptions e) {
            resultValue.setException("MissingPermissions");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
    }

    @PostMapping("/students/CV")
    public ResponseEntity<ResultValue<CurriculumVitaeDTO>> addCV(@RequestPart("file") MultipartFile uploadFile, HttpServletRequest request) {
        ResultValue<CurriculumVitaeDTO> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        Role role = loginService.getUserByEmail(jwtUsername).role();

        if (role != Role.STUDENT) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try {
            Date date = new Date();
            CurriculumVitaeDTO curriculumVitaeDTO = curriculumVitaeService.addCV(uploadFile, LocalDateTime.now(), jwtUsername);
            resultValue.setValue(curriculumVitaeDTO);
            return ResponseEntity.ok(resultValue);
        } catch (IOException e) {
            resultValue.setException("ReadingPDFFails");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultValue);
        }
    }

    @GetMapping("/students/CV/all")
    public ResponseEntity<PageResultValueList<CurriculumVitaeDTO>> getAllCVsFromUser(@RequestParam(name = "page") int pageNumber, @RequestParam(name = "size") int pageSize, HttpServletRequest request) {
        PageResultValueList<CurriculumVitaeDTO> resultValue = new PageResultValueList<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        UserDTO userDTO = loginService.getUserByEmail(jwtUsername);
        Role role = userDTO.role();

        if (role != Role.STUDENT) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        resultValue.setValue(curriculumVitaeService.getCVsByStudentId(userDTO.id(), pageNumber, pageSize));
        return ResponseEntity.ok(resultValue);
    }

    @GetMapping("/students/CV/validated")
    public ResponseEntity<ResultValue<List<CurriculumVitaeDTO>>> getAllValidatedCVsFromUser(HttpServletRequest request) {
        ResultValue<List<CurriculumVitaeDTO>> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        UserDTO userDTO = loginService.getUserByEmail(jwtUsername);
        Role role = userDTO.role();

        if (role != Role.STUDENT) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        resultValue.setValue(curriculumVitaeService.getValidatedCVsByStudentId(userDTO.id()));
        return ResponseEntity.ok(resultValue);
    }

    @PutMapping("/intershipmanager/validateCV/{cvId}")
    public ResponseEntity<ResultValue<CurriculumVitaeDTO>> validateCV(@PathVariable Long cvId, @RequestParam String approvalResult, HttpServletRequest request) {
        ResultValue<CurriculumVitaeDTO> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        UserDTO userDTO = loginService.getUserByEmail(jwtUsername);
        Role role = userDTO.role();

        if (role != Role.PROGRAM_MANAGER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try {
            CurriculumVitaeDTO validatedCV = curriculumVitaeService.validateCV(cvId, approvalResult, userDTO);
            resultValue.setValue(validatedCV);
            return ResponseEntity.ok(resultValue);
        } catch (CVNotFoundException e) {
            resultValue.setException("CVNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }


    @GetMapping("/intershipmanager/validateCV/discipline/{discipline}")
    public ResponseEntity<ResultValue<List<CurriculumVitaeDTO>>> getCVsByDiscipline(@PathVariable String discipline, HttpServletRequest request) {
        ResultValue<List<CurriculumVitaeDTO>> resultValue = new ResultValue<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        Role role = loginService.getUserByEmail(jwtUsername).role();

        if (role != Role.PROGRAM_MANAGER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try {
            List<CurriculumVitaeDTO> curriculumVitaeDTO = curriculumVitaeService.getCVsByDiscipline(discipline);
            resultValue.setValue(curriculumVitaeDTO);
            return ResponseEntity.ok(resultValue);
        } catch (DisciplineNotFoundException e) {
            resultValue.setException("DisciplineNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (CVNotFoundException e) {
            resultValue.setException("CVNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }

    @GetMapping("/cvs/{id}")
    public ResponseEntity<ResultValue<CurriculumVitaeDTO>> getCV(@PathVariable(name = "id") Long cvId,HttpServletRequest request) {
        ResultValue<CurriculumVitaeDTO> resultValue = new ResultValue<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        Role role = loginService.getUserByEmail(jwtUsername).role();

        if (role != Role.PROGRAM_MANAGER && role != Role.STUDENT) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try {
            resultValue.setValue(curriculumVitaeService.getCVById(cvId));
            return ResponseEntity.ok(resultValue);
        } catch (CVNotFoundException e) {
            resultValue.setException("CVNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }

    @GetMapping("/intershipmanager/validateCV/waitingforapproval")
    public ResponseEntity<ResultValue<List<CurriculumVitaeDTO>>> getCVsWaitingForApproval(HttpServletRequest request) {
        ResultValue<List<CurriculumVitaeDTO>> resultValue = new ResultValue<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        Role role = loginService.getUserByEmail(jwtUsername).role();

        if (role != Role.PROGRAM_MANAGER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try {
            List<CurriculumVitaeDTO> curriculumVitaeDTO = curriculumVitaeService.getCVsWaitingForApproval();
            resultValue.setValue(curriculumVitaeDTO);
            return ResponseEntity.ok(resultValue);
        } catch (CVNotFoundException e) {
            resultValue.setException("CVNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }

    @GetMapping("/contracts/")
    public ResponseEntity<PageResultValueList<InternshipOfferDTO>> getAwaitingInternships(@RequestParam(name = "season") String season,
                                                                                          @RequestParam(name = "year") String year,
                                                                                          @RequestParam(name = "page") int pageNumber,
                                                                                          @RequestParam(name = "size") int pageSize,
                                                                                          HttpServletRequest request) {
        PageResultValueList<InternshipOfferDTO> resultValue = new PageResultValueList<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        UserDTO userDTO = loginService.getUserByEmail(jwtUsername);
        Role role = userDTO.role();
        SessionDTO sessionDTO;
        try {
             sessionDTO = jobOfferService.getSessionFromDB(season, year);
        } catch (SessionNotFoundException e) {
            resultValue.setException("SessionNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }

        switch (role) {
            case STUDENT -> {
                resultValue.setValue(studentService.getContracts(userDTO.id(),sessionDTO.id(),pageNumber,pageSize));
            }
            case EMPLOYEUR -> {
                resultValue.setValue(employeurService.getContracts(userDTO.id(),sessionDTO.id(),pageNumber,pageSize));
            }
            case PROGRAM_MANAGER -> {
                resultValue.setValue(programManagerService.getContracts(sessionDTO.id(),pageNumber,pageSize));
            }
            default -> {
                resultValue.setException("Unauthorized");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
            }
        }
        return ResponseEntity.ok(resultValue);

    }

    @GetMapping("/internshipOffers/{id}")
    public ResponseEntity<ResultValue<InternshipOfferDTO>> getInternshipOffer(@PathVariable(name = "id") Long id,
                                                                                          HttpServletRequest request) {
        ResultValue<InternshipOfferDTO> resultValue = new ResultValue<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        UserDTO userDTO = loginService.getUserByEmail(jwtUsername);
        Role role = userDTO.role();

        if (!role.equals(Role.EMPLOYEUR) && !role.equals(Role.STUDENT) && !role.equals(Role.PROGRAM_MANAGER)) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try {
            resultValue.setValue(jobOfferService.getInternshipOffer(id));
            return ResponseEntity.ok(resultValue);
        } catch (InternshipNotFoundException e) {
            resultValue.setException("NotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }

    }

    @PostMapping("/contracts/sign/{internid}")
    public ResponseEntity<ResultValue<String>> sign(@PathVariable("internid") long internshipId, @RequestBody SignatureBase64DTO signatureDTO, HttpServletRequest request) {
        ResultValue<String> resultValue = new ResultValue<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        UserDTO userDTO;
        try {
            userDTO = loginService.getUserByEmailValidatePassword(jwtUsername,signatureDTO.extraData());
        } catch (InvalidPasswordException e) {
            resultValue.setException("Password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        Role role = userDTO.role();

        if (role != Role.PROGRAM_MANAGER && role != Role.EMPLOYEUR && role != Role.STUDENT) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try {
            resultValue.setValue(contractService.sign(userDTO,internshipId,signatureDTO).toString());
            return ResponseEntity.ok(resultValue);
        } catch (NumberFormatException | InternshipNotFoundException e) {
            resultValue.setException("NotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (MissingPermissionsExceptions e) {
            resultValue.setException("MissingPermissions");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultValue);
        } catch (InvalidBase64Exception | IOException e) {
            resultValue.setException("SignFormat");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultValue);
        } catch (JobApplicationNotFoundException e) {
            resultValue.setException("AlreadySigned");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultValue);
        }
    }

    @PostMapping("/internshipManager/contracts/begin/{id}")
    public ResponseEntity<ResultValue<String>> beginContractProcess(@PathVariable("id") String internshipIdString, HttpServletRequest request) {
        ResultValue<String> resultValue = new ResultValue<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        UserDTO userDTO = loginService.getUserByEmail(jwtUsername);
        Role role = userDTO.role();

        if (role != Role.PROGRAM_MANAGER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try {
            long internshipId = Long.parseLong(internshipIdString);

            programManagerService.beginContract(internshipId,userDTO);
            resultValue.setValue("ok");
            return ResponseEntity.ok(resultValue);
        } catch (NumberFormatException | InternshipNotFoundException | JobApplicationNotFoundException e) {
            resultValue.setException("NotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }

    // eq6-128
    @GetMapping("/print/contracts/{internshipId}")
    public ResponseEntity<ResultValue<byte[]>> printContract(@PathVariable Long internshipId, HttpServletRequest request){
        ResultValue<byte[]> resultValue = new ResultValue<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        UserDTO user = loginService.getUserByEmail(jwtUsername);
        Role role = user.role();
        if (role != Role.PROGRAM_MANAGER && role != Role.EMPLOYEUR && role != Role.STUDENT) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try {
            byte[] contract = pdfService.printContract(internshipId, user);
            resultValue.setValue(contract);
            return ResponseEntity.ok(resultValue);
        } catch (InternshipNotFoundException e) {
            resultValue.setException("InternshipNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (MissingPermissionsExceptions e) {
            resultValue.setException("MissingPermissions");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        } catch (IOException e) {
            resultValue.setException("IOException");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultValue);
        }
    }

    // eq6-128
    @GetMapping("/print/evaluationEmployer/{evaluationId}")
    public ResponseEntity<ResultValue<byte[]>> printEvaluationEmployer(@PathVariable Long evaluationId, HttpServletRequest request){
        ResultValue<byte[]> resultValue = new ResultValue<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        UserDTO user = loginService.getUserByEmail(jwtUsername);
        Role role = user.role();
        if (role != Role.PROGRAM_MANAGER && role != Role.TEACHER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try {
            byte[] contract = pdfService.printEmployerEvaluation(evaluationId, user);
            resultValue.setValue(contract);
            return ResponseEntity.ok(resultValue);
        }  catch (IOException e) {
            resultValue.setException("IOException");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultValue);
        } catch (EvaluationNotFoundException e) {
            resultValue.setException("EvaluationNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (MissingPermissionsExceptions e) {
            resultValue.setException("MissingPermissions");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

    }


    // eq6-128
    @GetMapping("/print/evaluationIntern/{internshipOfferId}")
    public ResponseEntity<ResultValue<byte[]>> printEvaluationIntern(@PathVariable Long internshipOfferId, HttpServletRequest request){
        ResultValue<byte[]> resultValue = new ResultValue<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        UserDTO user = loginService.getUserByEmail(jwtUsername);
        Role role = user.role();
        if (role != Role.PROGRAM_MANAGER && role != Role.EMPLOYEUR && role != Role.TEACHER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try {
            byte[] contract = pdfService.printInternEvaluation(internshipOfferId,user.id());
            resultValue.setValue(contract);
            return ResponseEntity.ok(resultValue);
        } catch (IOException e) {
            resultValue.setException("IOException");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultValue);
        } catch (EvaluationNotFoundException e) {
            resultValue.setException("EvaluationNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (MissingPermissionsExceptions e) {
            resultValue.setException("MissingPermissions");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
    }


    // EQ6-26 get prof
    @GetMapping("/internshipManager/profs/all")
    public ResponseEntity<ResultValue<List<TeacherDTO>>> geAlltProfs(HttpServletRequest request) {
        ResultValue<List<TeacherDTO>> resultValue = new ResultValue<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        Role role = loginService.getUserByEmail(jwtUsername).role();

        if (role != Role.PROGRAM_MANAGER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try {
            List<TeacherDTO> teacherDTO = teacherService.getAllProfs();
            resultValue.setValue(teacherDTO);
            return ResponseEntity.ok(resultValue);
        } catch (UserNotFoundException e) {
            resultValue.setException("NoProfsFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }

    @GetMapping("/internshipManager/profs/{id}/currentInterns")
    public ResponseEntity<ResultValue<List<InternshipEvaluationDTO>>> getProfCurrentInterns(@PathVariable("id") Long profId, HttpServletRequest request) {
        ResultValue<List<InternshipEvaluationDTO>> resultValue = new ResultValue<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        Role role = loginService.getUserByEmail(jwtUsername).role();

        if (role != Role.PROGRAM_MANAGER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        List<InternshipEvaluationDTO> internshipEvaluationDTO = programManagerService.getProfCurrentInterns(profId);
        resultValue.setValue(internshipEvaluationDTO);
        return ResponseEntity.ok(resultValue);

    }



    // EQ6-26 get students who already signed contract but waiting for evaluation
    @GetMapping("/internshipManager/interns/waitingForAssignmentToProf")
    public ResponseEntity<ResultValue<List<InternshipOfferDTO>>> getInternsWaitingForAssignmentToProf(HttpServletRequest request) {
        ResultValue<List<InternshipOfferDTO>> resultValue = new ResultValue<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        Role role = loginService.getUserByEmail(jwtUsername).role();

        if (role != Role.PROGRAM_MANAGER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try {
            List<InternshipOfferDTO> intershipOffers = programManagerService.getInternsWaitingForAssignmentToProf();
            resultValue.setValue(intershipOffers);
            return ResponseEntity.ok(resultValue);
        } catch (InternshipNotFoundException e) {
            resultValue.setException("NoInternsWaitingForAssignmentToProf");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }


    // EQ6-26 create internship evaluation
    @PostMapping("/internshipManager/interns/assignToProf")
    public ResponseEntity<ResultValue<InternshipEvaluationDTO>> assignToProf(@RequestParam Long profId, @RequestParam Long internshipOfferId, HttpServletRequest request){
        ResultValue<InternshipEvaluationDTO> resultValue = new ResultValue<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        UserDTO userDTO = loginService.getUserByEmail(jwtUsername);
        Role role = userDTO.role();

        if (role != Role.PROGRAM_MANAGER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try {
            InternshipEvaluationDTO internshipEvaluationDTO = programManagerService.assignToProf(profId, internshipOfferId,userDTO);
            resultValue.setValue(internshipEvaluationDTO);
            return ResponseEntity.ok(resultValue);
        } catch (InternshipNotFoundException e) {
            resultValue.setException("InternshipNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (UserNotFoundException e) {
            resultValue.setException("ProfNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }

    // EQ6-27
    @GetMapping("/teacher/students")
    public ResponseEntity<ResultValue<List<StudentDTO>>> getStudents(HttpServletRequest request) {
        ResultValue<List<StudentDTO>> resultValue = new ResultValue<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        UserDTO userDTO;
        try {
            userDTO = loginService.getUserByEmail(jwtUsername);
        } catch (UserNotFoundException e) {
            resultValue.setException("UserNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }

        Role role = userDTO.role();
        if (role != Role.TEACHER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try {
            List<StudentDTO> studentDTO = teacherService.getStudentsByTeacherId(userDTO.id());
            resultValue.setValue(studentDTO);
            return ResponseEntity.ok(resultValue);
        } catch (UserNotFoundException e) {
            resultValue.setException("ProfNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }



    @GetMapping("/download/file/{filename}")
    public ResponseEntity<ResultValue<byte[]>> getPDF(@PathVariable String filename, HttpServletRequest request) {
        ResultValue<byte[]> resultValue = new ResultValue<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        // TODO add reading file permission

        try {
            byte[] pdf = curriculumVitaeService.getPDF(filename);
            resultValue.setValue(pdf);
            return ResponseEntity.ok(resultValue);
        } catch (FileNotFoundException e) {
            resultValue.setException("FileNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }

    @PutMapping("/notifications/read")
    public ResponseEntity<ResultValue<String>> viewNotificationsList(@RequestBody List<Long> ids, HttpServletRequest request) {
        ResultValue<String> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        UserDTO userDTO;
        try {
            userDTO = loginService.getUserByEmail(jwtUsername);
        } catch (UserNotFoundException e) {
            resultValue.setException("UserNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }

        for (Long id : ids) {
            try {
                if (id == null) {
                    resultValue.setException("NotificationNotFound"); // Ceci règle techniquement pas le bug qui existe dans ValidateOffer FE, mais sashant que le bug n'a pas d'autres effets à cause de TryFindViewNotification(), ceci cache juste l'exception de la console.
                } else {
                    notificationService.viewNotification(userDTO, id);
                    resultValue.setValue("ok");
                }
            } catch (NotificationNotFoundException e) {
                resultValue.setException("NotificationNotFound");

            }
        }
        if (resultValue.getValue() == null || resultValue.getValue().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
        return ResponseEntity.ok(resultValue);
    }

    @GetMapping("/notifications/unread")
    public ResponseEntity<ResultValue<NotificationRootDTO>> getUnreadNotifications(@RequestParam(name = "season") String season,
                                                                                   @RequestParam(name = "year") String year,
                                                                                   HttpServletRequest request) {
        ResultValue<NotificationRootDTO> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        try {
            SessionDTO sessionDTO = jobOfferService.getSessionFromDB(season, year);
            resultValue.setValue(notificationService.getUnreadNotifications(jwtUsername,sessionDTO.id()));
            return ResponseEntity.ok(resultValue);
        } catch (UserNotFoundException e) {
            resultValue.setException("NotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (SessionNotFoundException e) {
            resultValue.setException("SessionNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }

    @GetMapping("/academicSessions")
    public ResponseEntity<ResultValue<List<SessionDTO>>> getListAcademicSessions(HttpServletRequest request) {
        ResultValue<List<SessionDTO>> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        resultValue.setValue(jobOfferService.getAcademicSessions());
        return ResponseEntity.ok(resultValue);
    }

    @GetMapping("/currentAcademicSession")
    public ResponseEntity<ResultValue<SessionDTO>> getCurrentAcademicSession(HttpServletRequest request) {
        ResultValue<SessionDTO> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        resultValue.setValue(programManagerService.getCurrentAcademicSessionDTO());
        return ResponseEntity.ok(resultValue);
    }

    private ResponseEntity<ResultValue<JobInterviewDTO>> getResultValueResponseEntity(JobInterviewRegisterDTO jobInterviewRegisterDTO, UserDTO userDTO, ResultValue<JobInterviewDTO> resultValue) {
        try {
            JobInterviewDTO jobInterviewDTO = employeurService.createJobInterview(jobInterviewRegisterDTO, userDTO);
            resultValue.setValue(jobInterviewDTO);
            return ResponseEntity.ok(resultValue);
        } catch (InvalideInterviewTypeException e) {
            resultValue.setException("InvalidInterviewType");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultValue);
        } catch (JobApplicationNotFoundException e) {
            resultValue.setException("JobApplicationNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (JobApplicationNotActiveException e) {
            resultValue.setException("JobApplicationNotActive");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultValue);
        } catch (MissingPermissionsExceptions e) {
            resultValue.setException("JobApplicationNotYours");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        } catch (IllegalArgumentException e) {
            resultValue.setException("InvalidDateTimeFormat");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultValue);
        } catch (NullPointerException e) {
            resultValue.setException("ContainsNullFields");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultValue);
        }
    }
    @GetMapping("/teacher/internshipEvaluations")
    public ResponseEntity<ResultValue<List<InternshipEvaluationDTO>>> getInternshipEvaluations(
            @RequestParam(name = "season") String season,
            @RequestParam(name = "year") String year,
            HttpServletRequest request) {
        ResultValue<List<InternshipEvaluationDTO>> resultValue = new ResultValue<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        Role role = loginService.getUserByEmail(jwtUsername).role();

        UserDTO userDTO;
        SessionDTO sessionDTO;
        try {
            userDTO = loginService.getUserByEmail(jwtUsername);
            sessionDTO = jobOfferService.getSessionFromDB(season, year);
        } catch (UserNotFoundException e) {
            resultValue.setException("UserNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (SessionNotFoundException e){
            resultValue.setException("SessionNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }

        if (role != Role.TEACHER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }


        try {
            List<InternshipEvaluationDTO> internshipEvaluations = teacherService.getEmployeurByTeacher(userDTO.id(), sessionDTO.id());
            resultValue.setValue(internshipEvaluations);
            return ResponseEntity.ok(resultValue);
        } catch (Exception e) {
            resultValue.setException("EvaluationsNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }

    }

    @GetMapping("/employer/students")
    public ResponseEntity<ResultValue<List<InternshipOfferDTO>>> EmployerByStudents(
            @RequestParam(name = "season") String season,
            @RequestParam(name = "year") String year,
            HttpServletRequest request) {
        ResultValue<List<InternshipOfferDTO>> resultValue = new ResultValue<>();
        String token = request.getHeader("Authorization");

        // Extract JWT token
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        UserDTO userDTO;
        SessionDTO sessionDTO;
        try {
            userDTO = loginService.getUserByEmail(jwtUsername);
            sessionDTO = jobOfferService.getSessionFromDB(season, year);
        } catch (UserNotFoundException e) {
            resultValue.setException("UserNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (SessionNotFoundException e){
            resultValue.setException("SessionNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }

        Role role = userDTO.role();
        if (role != Role.EMPLOYEUR) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try {
            List<InternshipOfferDTO> studentOffers = employeurService.getStudentsWithOffersByEmployerId(userDTO.id(), sessionDTO.id());
            resultValue.setValue(studentOffers);
            return ResponseEntity.ok(resultValue);
        } catch (UserNotFoundException e) {
            resultValue.setException("ProfNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }

    // returns basic job offers stats for all job offers employer created
    @GetMapping("/employeur/jobOffers/stats")
    public ResponseEntity<ResultValue<Map<Long, JobOfferStatsDTO>>> getJobOffersStats(
            @RequestParam(name = "season") String season,
            @RequestParam(name = "year") String year,
            HttpServletRequest request) {
        ResultValue<Map<Long, JobOfferStatsDTO>> resultValue = new ResultValue<>();
        String token = request.getHeader("Authorization");

        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        UserDTO userDTO;
        SessionDTO sessionDTO;
        try {
            userDTO = loginService.getUserByEmail(jwtUsername);
            sessionDTO = jobOfferService.getSessionFromDB(season, year);
        } catch (UserNotFoundException e) {
            resultValue.setException("UserNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        } catch (SessionNotFoundException e){
            resultValue.setException("SessionNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }

        Role role = userDTO.role();
        if (role != Role.EMPLOYEUR) {
            resultValue.setException("User is not authorized to access this resource");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try {
            Map<Long, JobOfferStatsDTO> jobOffersStatsDTO = jobOfferService.getJobOffersStats(userDTO.id(), sessionDTO.id());
            resultValue.setValue(jobOffersStatsDTO);
            return ResponseEntity.ok(resultValue);
        }
        catch (Exception e) {
            resultValue.setException("Error getting job offers stats");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultValue);
        }
    }

    @PostMapping("/teacher/evaluations/{id}")
    public ResponseEntity<ResultValue<EvaluationEmployerDTO>> createEvaluation(@PathVariable Long id,  @RequestBody EvaluationEmployerDTO evaluationDTO, HttpServletRequest request) {
        ResultValue<EvaluationEmployerDTO> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            logger.error("Invalid JWT token");
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        UserDTO userDTO = loginService.getUserByEmail(jwtUsername);

        if (userDTO.role() != Role.TEACHER) {
            resultValue.setException("User is not authorized to access this resource");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try {
            EvaluationEmployerDTO createdEvaluation = EvaluationEmployerDTO.toDTO(evaluationEmployerService.createEvaluation(id, evaluationDTO, userDTO));
            resultValue.setValue(createdEvaluation);
            return ResponseEntity.status(HttpStatus.OK).body(resultValue);
        } catch (Exception e) {
            resultValue.setException("Error creating evaluation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultValue);
        }
    }

    @PostMapping("employer/evaluations/{id}")
    public ResponseEntity<ResultValue<EvaluationInternDTO>> createEvaluationInternEvaluation(
            @PathVariable Long id,
            HttpServletRequest request, @RequestBody EvaluationInternDTO evaluationDTO) {
        ResultValue<EvaluationInternDTO> resultValue = new ResultValue<>();

        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);

        if (jwtUsername == null) {
            resultValue.setException("Invalid JWT Token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }

        UserDTO userDTO;
        try {
            userDTO = loginService.getUserByEmail(jwtUsername);
        } catch (UserNotFoundException e) {
            resultValue.setException("UserNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }

        Role role = userDTO.role();
        if (role != Role.EMPLOYEUR && role != Role.TEACHER && role != Role.PROGRAM_MANAGER) { //??? - Nathan_Nino
            resultValue.setException("Forbidden");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }

        try {
            EvaluationInternDTO createdEvaluation = employeurService.saveEvaluation(id, evaluationDTO, userDTO);
            resultValue.setValue(createdEvaluation);
            return ResponseEntity.status(HttpStatus.CREATED).body(resultValue);
        } catch (InternshipNotFoundException e) {
            resultValue.setException("EvaluationNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }catch (Exception e) {
            resultValue.setException("Error creating evaluation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultValue);
        }
    }

    @GetMapping("internshipManager/nonValidatedOffers")
    public ResponseEntity<PageResultValueList<JobOfferDTO>> getNonValidatedOffers(HttpServletRequest request,
                                                                                    @RequestParam("season") String season,
                                                                                    @RequestParam("year") String year,
                                                                                    @RequestParam("page") int pageNumber,
                                                                                    @RequestParam("size") int pageSize,
                                                                                    @RequestParam(name = "q", required = false) String query) {
        if(query == null) query = "";
        PageResultValueList<JobOfferDTO> resultValue = new PageResultValueList<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        Role role = loginService.getUserByEmail(jwtUsername).role();
        if (role != Role.PROGRAM_MANAGER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try {
            SessionDTO sessionDTO = jobOfferService.getSessionFromDB(season, year);
            Pageable pageable = PageRequest.of(pageNumber,pageSize);
            Page<JobOfferDTO> internshipOfferDTO = programManagerService.getAllNonApprovedJobOffers(pageable, query, sessionDTO.id());
            resultValue.setValue(internshipOfferDTO);
            return ResponseEntity.ok(resultValue);
        } catch (UserNotFoundException e) {
            resultValue.setException("NoOffersFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }

    @GetMapping("internshipManager/validatedOffers")
    public ResponseEntity<PageResultValueList<JobOfferDTO>> getValidatedInternshipOffers(HttpServletRequest request,
                                                                                         @RequestParam("season") String season,
                                                                                         @RequestParam("year") String year,
                                                                                    @RequestParam("page") int pageNumber,
                                                                                    @RequestParam("size") int pageSize,
                                                                                    @RequestParam(name = "q", required = false) String query) {
        if(query == null) query = "";
        PageResultValueList<JobOfferDTO> resultValue = new PageResultValueList<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        Role role = loginService.getUserByEmail(jwtUsername).role();
        if (role != Role.PROGRAM_MANAGER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try {
            SessionDTO sessionDTO = jobOfferService.getSessionFromDB(season, year);
            Pageable pageable = PageRequest.of(pageNumber,pageSize);
            Page<JobOfferDTO> jobOfferDTOS = programManagerService.getAllApprovedJobOffers(pageable, query, sessionDTO.id());
            resultValue.setValue(jobOfferDTOS);
            return ResponseEntity.ok(resultValue);
        } catch (UserNotFoundException e) {
            resultValue.setException("NoOffersFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }

    @GetMapping("internshipManager/signedUpStudents")
    public ResponseEntity<PageResultValueList<StudentDTO>> getAllStudents(HttpServletRequest request,
                                                                          @RequestParam("season") String season,
                                                                          @RequestParam("year") String year,
                                                                        @RequestParam("page") int pageNumber,
                                                                        @RequestParam("size") int pageSize,
                                                                        @RequestParam(name = "q", required = false) String query) {
        if(query == null) query = "";
        PageResultValueList<StudentDTO> resultValue = new PageResultValueList<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        Role role = loginService.getUserByEmail(jwtUsername).role();
        if (role != Role.PROGRAM_MANAGER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try {
            SessionDTO sessionDTO = jobOfferService.getSessionFromDB(season, year);
            Pageable pageable = PageRequest.of(pageNumber,pageSize);
            Page<StudentDTO> studentDTOS = programManagerService.getAllStudents(pageable, query, sessionDTO.id());
            resultValue.setValue(studentDTOS);
            return ResponseEntity.ok(resultValue);
        } catch (UserNotFoundException e) {
            resultValue.setException("NoOffersFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }

    @GetMapping("internshipManager/studentsNoCV")
    public ResponseEntity<PageResultValueList<StudentDTO>> getAllStudentsNoCV(HttpServletRequest request,
                                                                              @RequestParam("season") String season,
                                                                              @RequestParam("year") String year,
                                                                        @RequestParam("page") int pageNumber,
                                                                        @RequestParam("size") int pageSize,
                                                                        @RequestParam(name = "q", required = false) String query) {
        if(query == null) query = "";
        PageResultValueList<StudentDTO> resultValue = new PageResultValueList<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        Role role = loginService.getUserByEmail(jwtUsername).role();
        if (role != Role.PROGRAM_MANAGER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try {
            SessionDTO sessionDTO = jobOfferService.getSessionFromDB(season, year);
            Pageable pageable = PageRequest.of(pageNumber,pageSize);
            Page<StudentDTO> studentDTOS = programManagerService.getAllStudentsWithNoCV(pageable, query, sessionDTO.id());
            resultValue.setValue(studentDTOS);
            return ResponseEntity.ok(resultValue);
        } catch (UserNotFoundException e) {
            resultValue.setException("NoOffersFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }

    @GetMapping("internshipManager/studentsCVNotValidated")
    public ResponseEntity<PageResultValueList<StudentDTO>> getAllStudentsCVNotAccepted(HttpServletRequest request,
                                                                                       @RequestParam("season") String season,
                                                                                       @RequestParam("year") String year,
                                                                                    @RequestParam("page") int pageNumber,
                                                                                    @RequestParam("size") int pageSize,
                                                                                    @RequestParam(name = "q", required = false) String query) {
        if(query == null) query = "";
        PageResultValueList<StudentDTO> resultValue = new PageResultValueList<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        Role role = loginService.getUserByEmail(jwtUsername).role();
        if (role != Role.PROGRAM_MANAGER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try {
            SessionDTO sessionDTO = jobOfferService.getSessionFromDB(season, year);
            Pageable pageable = PageRequest.of(pageNumber,pageSize);
            Page<StudentDTO> studentDTOS = programManagerService.getAllStudentsWithCVNotAccepted(pageable, query, sessionDTO.id());
            resultValue.setValue(studentDTOS);
            return ResponseEntity.ok(resultValue);
        } catch (UserNotFoundException e) {
            resultValue.setException("NoOffersFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }

    @GetMapping("internshipManager/studentsNoInterview")
    public ResponseEntity<PageResultValueList<StudentDTO>> getAllStudentsNoInterview(HttpServletRequest request,
                                                                                     @RequestParam("season") String season,
                                                                                     @RequestParam("year") String year,
                                                                                             @RequestParam("page") int pageNumber,
                                                                                             @RequestParam("size") int pageSize,
                                                                                             @RequestParam(name = "q", required = false) String query) {
        if(query == null) query = "";
        PageResultValueList<StudentDTO> resultValue = new PageResultValueList<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        Role role = loginService.getUserByEmail(jwtUsername).role();
        if (role != Role.PROGRAM_MANAGER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try {
            SessionDTO sessionDTO = jobOfferService.getSessionFromDB(season, year);
            Pageable pageable = PageRequest.of(pageNumber,pageSize);
            Page<StudentDTO> studentDTOS = programManagerService.getAllStudentsWithNoInterview(pageable, query, sessionDTO.id());
            resultValue.setValue(studentDTOS);
            return ResponseEntity.ok(resultValue);
        } catch (UserNotFoundException e) {
            resultValue.setException("NoOffersFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }

    @GetMapping("internshipManager/studentsAwaitingInterview")
    public ResponseEntity<PageResultValueList<StudentDTO>> getAllStudentsWaitingForInterview(HttpServletRequest request,
                                                                                             @RequestParam("season") String season,
                                                                                             @RequestParam("year") String year,
                                                                                           @RequestParam("page") int pageNumber,
                                                                                           @RequestParam("size") int pageSize,
                                                                                           @RequestParam(name = "q", required = false) String query) {
        if(query == null) query = "";
        PageResultValueList<StudentDTO> resultValue = new PageResultValueList<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        Role role = loginService.getUserByEmail(jwtUsername).role();
        if (role != Role.PROGRAM_MANAGER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try {
            SessionDTO sessionDTO = jobOfferService.getSessionFromDB(season, year);
            Pageable pageable = PageRequest.of(pageNumber,pageSize);
            Page<StudentDTO> studentDTOS = programManagerService.getAllStudentsWaitingForInterview(pageable, query, sessionDTO.id());
            resultValue.setValue(studentDTOS);
            return ResponseEntity.ok(resultValue);
        } catch (UserNotFoundException e) {
            resultValue.setException("NoOffersFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }

    @GetMapping("internshipManager/studentsAwaitingInterviewResponse")
    public ResponseEntity<PageResultValueList<StudentDTO>> getAllStudentsWaitingForInterviewAnswer(HttpServletRequest request,
                                                                                                   @RequestParam("season") String season,
                                                                                                   @RequestParam("year") String year,
                                                                                                   @RequestParam("page") int pageNumber,
                                                                                                   @RequestParam("size") int pageSize,
                                                                                                   @RequestParam(name = "q", required = false) String query) {
        if(query == null) query = "";
        PageResultValueList<StudentDTO> resultValue = new PageResultValueList<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        Role role = loginService.getUserByEmail(jwtUsername).role();
        if (role != Role.PROGRAM_MANAGER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try {
            SessionDTO sessionDTO = jobOfferService.getSessionFromDB(season, year);
            Pageable pageable = PageRequest.of(pageNumber,pageSize);
            Page<StudentDTO> studentDTOS = programManagerService.
                    getAllStudentsWaitingForInterviewAnswer(pageable, query, sessionDTO.id());
            resultValue.setValue(studentDTOS);
            return ResponseEntity.ok(resultValue);
        } catch (JobApplicationNotFoundException e) {
            resultValue.setException("NoOffersFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }

    @GetMapping("internshipManager/studentsWhoFoundInternship")
    public ResponseEntity<PageResultValueList<StudentDTO>> getAllStudentsWithInternship(HttpServletRequest request,
                                                                                        @RequestParam("season") String season,
                                                                                        @RequestParam("year") String year,
                                                                                              @RequestParam("page") int pageNumber,
                                                                                              @RequestParam("size") int pageSize,
                                                                                              @RequestParam(name = "q", required = false) String query) {
        if(query == null) query = "";
        PageResultValueList<StudentDTO> resultValue = new PageResultValueList<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        Role role = loginService.getUserByEmail(jwtUsername).role();
        if (role != Role.PROGRAM_MANAGER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try {
            SessionDTO sessionDTO = jobOfferService.getSessionFromDB(season, year);
            Pageable pageable = PageRequest.of(pageNumber,pageSize);
            Page<StudentDTO> studentDTOS = programManagerService.getAllStudentsWithInternship(pageable, query, sessionDTO.id());
            resultValue.setValue(studentDTOS);
            return ResponseEntity.ok(resultValue);
        } catch (JobApplicationNotFoundException e) {
            resultValue.setException("NoOffersFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }

    @GetMapping("internshipManager/studentsNotEvaluatedBySupervisor")
    public ResponseEntity<PageResultValueList<StudentDTO>> getAllStudentsNotYetAssessedByTeacher(HttpServletRequest request,
                                                                                                 @RequestParam("season") String season,
                                                                                                 @RequestParam("year") String year,
                                                                                              @RequestParam("page") int pageNumber,
                                                                                              @RequestParam("size") int pageSize,
                                                                                              @RequestParam(name = "q", required = false) String query) {
        if(query == null) query = "";
        PageResultValueList<StudentDTO> resultValue = new PageResultValueList<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        Role role = loginService.getUserByEmail(jwtUsername).role();
        if (role != Role.PROGRAM_MANAGER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try {
            SessionDTO sessionDTO = jobOfferService.getSessionFromDB(season, year);
            Pageable pageable = PageRequest.of(pageNumber,pageSize);
            Page<StudentDTO> studentDTOS = programManagerService.getAllStudentsNotYetAssessedByTeacher(pageable, query, sessionDTO.id());
            resultValue.setValue(studentDTOS);
            return ResponseEntity.ok(resultValue);
        } catch (JobApplicationNotFoundException e) {
            resultValue.setException("NoOffersFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }

    @GetMapping("internshipManager/studentsSupervisorHasntEvaluatedEnterprise")
    public ResponseEntity<PageResultValueList<StudentDTO>> getAllStudentsNotYetAssessedByInternManager(HttpServletRequest request,
                                                                                                       @RequestParam("season") String season,
                                                                                                       @RequestParam("year") String year,
                                                                                                       @RequestParam("page") int pageNumber,
                                                                                                       @RequestParam("size") int pageSize,
                                                                                                       @RequestParam(name = "q", required = false) String query) {
        if(query == null) query = "";
        PageResultValueList<StudentDTO> resultValue = new PageResultValueList<>();
        String token = request.getHeader("Authorization");
        String jwtUsername = jwtTokenProvider.getJwtUsername(token);
        if (jwtUsername == null) {
            resultValue.setException("JWT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultValue);
        }
        Role role = loginService.getUserByEmail(jwtUsername).role();
        if (role != Role.PROGRAM_MANAGER) {
            resultValue.setException("Unauthorized");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultValue);
        }
        try {
            SessionDTO sessionDTO = jobOfferService.getSessionFromDB(season, year);
            Pageable pageable = PageRequest.of(pageNumber,pageSize);
            Page<StudentDTO> studentDTOS = programManagerService.getAllStudentsWithEmployerNotYetAssessedBySupervisor(pageable, query, sessionDTO.id());
            resultValue.setValue(studentDTOS);
            return ResponseEntity.ok(resultValue);
        } catch (JobApplicationNotFoundException e) {
            resultValue.setException("NoOffersFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultValue);
        }
    }


}
