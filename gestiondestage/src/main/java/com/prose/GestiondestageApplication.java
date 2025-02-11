package com.prose;

import com.prose.service.CurriculumVitaeService;
import com.prose.service.ProgramManagerService;
import com.prose.service.StudentService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class GestiondestageApplication {

	private final StudentService studentService;
	private final CurriculumVitaeService curriculumVitaeService;

	private final ProgramManagerService programManagerService;

    public GestiondestageApplication(StudentService studentService, CurriculumVitaeService curriculumVitaeService, ProgramManagerService programManagerService) {
        this.studentService = studentService;
        this.curriculumVitaeService = curriculumVitaeService;
		this.programManagerService = programManagerService;
    }


    public static void main(String[] args) {
		SpringApplication.run(GestiondestageApplication.class, args);
	}
}
