package com.prose.security;

import com.prose.entity.users.auth.Role;
import com.prose.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final UserAppRepository userAppRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth

                        // All
                        .requestMatchers(GET, "/disciplines").permitAll()
                        .requestMatchers(POST, "/inscription/*").permitAll()
                        .requestMatchers(POST, "/connect").permitAll()
                        .requestMatchers(GET, "/userinfo/username").hasAnyAuthority(Role.STUDENT.toString(),Role.TEACHER.toString(),Role.EMPLOYEUR.toString(),Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(GET, "/userinfo/myProfile").hasAnyAuthority(Role.STUDENT.toString(),Role.TEACHER.toString(),Role.EMPLOYEUR.toString(),Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(PUT, "/userinfo/teacher/*").hasAnyAuthority(Role.STUDENT.toString(),Role.TEACHER.toString(),Role.EMPLOYEUR.toString(),Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(PUT, "/userinfo/student/*").hasAnyAuthority(Role.STUDENT.toString(),Role.TEACHER.toString(),Role.EMPLOYEUR.toString(),Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(PUT, "/userinfo/employeur/*").hasAnyAuthority(Role.STUDENT.toString(),Role.TEACHER.toString(),Role.EMPLOYEUR.toString(),Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(PUT, "/userinfo/projet_manager/*").hasAnyAuthority(Role.STUDENT.toString(),Role.TEACHER.toString(),Role.EMPLOYEUR.toString(),Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(PUT, "/userinfo/teacher/password/*").hasAnyAuthority(Role.STUDENT.toString(),Role.TEACHER.toString(),Role.EMPLOYEUR.toString(),Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(PUT, "/userinfo/student/password/*").hasAnyAuthority(Role.STUDENT.toString(),Role.TEACHER.toString(),Role.EMPLOYEUR.toString(),Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(PUT, "/userinfo/employeur/password/*").hasAnyAuthority(Role.STUDENT.toString(),Role.TEACHER.toString(),Role.EMPLOYEUR.toString(),Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(PUT, "/userinfo/projet_manager/password/*").hasAnyAuthority(Role.STUDENT.toString(),Role.TEACHER.toString(),Role.EMPLOYEUR.toString(),Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(GET, "/notifications").hasAnyAuthority(Role.STUDENT.toString(),Role.TEACHER.toString(),Role.EMPLOYEUR.toString(),Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(GET, "/notifications/unread").hasAnyAuthority(
                                Role.STUDENT.toString(),
                                Role.TEACHER.toString(),
                                Role.EMPLOYEUR.toString(),
                                Role.PROGRAM_MANAGER.toString()
                        )
                        .requestMatchers(GET, "/notifications/unread/count").hasAnyAuthority(
                                Role.STUDENT.toString(),
                                Role.TEACHER.toString(),
                                Role.EMPLOYEUR.toString(),
                                Role.PROGRAM_MANAGER.toString()
                        )
                        .requestMatchers(PUT, "/notifications/read").hasAnyAuthority(Role.STUDENT.toString(),Role.TEACHER.toString(),Role.EMPLOYEUR.toString(),Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(GET, "/download/file/*").hasAnyAuthority(Role.STUDENT.toString(),Role.TEACHER.toString(),Role.EMPLOYEUR.toString(),Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(GET, "/academicSessions").hasAnyAuthority(Role.STUDENT.toString(),Role.TEACHER.toString(),Role.EMPLOYEUR.toString(),Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(GET, "/currentAcademicSession").hasAnyAuthority(Role.STUDENT.toString(),Role.TEACHER.toString(),Role.EMPLOYEUR.toString(),Role.PROGRAM_MANAGER.toString())

                        // Student
                        .requestMatchers(POST, "/students/CV").hasAuthority(Role.STUDENT.toString())
                        .requestMatchers(GET, "/students/CV/all").hasAuthority(Role.STUDENT.toString()) // Contient pagination
                        .requestMatchers(GET, "/students/CV/validated").hasAuthority(Role.STUDENT.toString()) // TODO : Ne peux pas être paginé
                        .requestMatchers(GET, "/student/jobOffers").hasAuthority(Role.STUDENT.toString()) // Contient pagination + filtre DONE IN FE
                        .requestMatchers(POST, "/student/jobOffers/apply").hasAuthority(Role.STUDENT.toString())
                        .requestMatchers(GET, "/student/applications").hasAuthority(Role.STUDENT.toString()) // Ne peux pas être paginé
                        .requestMatchers(POST, "/student/applications/cancel/*").hasAuthority(Role.STUDENT.toString())
                        .requestMatchers(GET, "/student/jobInterview").hasAuthority(Role.STUDENT.toString()) // Contient pagination + filtre DONE IN FE
                        .requestMatchers(POST, "/student/jobInterview/confirmation/*").hasAuthority(Role.STUDENT.toString())
                        .requestMatchers(GET, "/student/internshipOffers").hasAuthority(Role.STUDENT.toString()) // Contient pagination + filtre DONE IN FE
                        .requestMatchers(PUT, "/student/internshipOffers/*/confirmation").hasAuthority(Role.STUDENT.toString())

                        // Employer
                        .requestMatchers(GET, "/employeur/jobOffers").hasAuthority(Role.EMPLOYEUR.toString()) // N'a pas besoin de pagination
                        .requestMatchers(POST, "/employeur/jobOffers").hasAuthority(Role.EMPLOYEUR.toString())
                        .requestMatchers(PUT, "/employeur/jobOffers/*").hasAuthority(Role.EMPLOYEUR.toString())
                        .requestMatchers(GET, "/employeur/jobInterview").hasAuthority(Role.EMPLOYEUR.toString()) // Contient pagination + filtre DONE IN FE
                        .requestMatchers(GET, "/employeur/jobOffers/listeJobApplications/*").hasAuthority(Role.EMPLOYEUR.toString()) // Contient pagination // TODO : Ajouter filtre?? // DONE IN FE BUT NO SEARCH BAR
                        .requestMatchers(GET, "/employeur/jobApplications/*").hasAuthority(Role.EMPLOYEUR.toString()) // Contient pagination // TODO : Ajouter filtre?? // DONE IN FE BUT NO SEARCH BAR
                        .requestMatchers(POST, "/employeur/jobInterview").hasAuthority(Role.EMPLOYEUR.toString())
                        .requestMatchers(DELETE, "/employeur/jobInterview/*").hasAuthority(Role.EMPLOYEUR.toString())
                        .requestMatchers(PUT, "/employeur/offerInternshipToStudent").hasAuthority(Role.EMPLOYEUR.toString())
                        .requestMatchers(GET, "/employer/students").hasAnyAuthority(Role.EMPLOYEUR.toString())
                        .requestMatchers(GET, "/employeur/jobOffers/stats").hasAnyAuthority(Role.EMPLOYEUR.toString())

                        // Teacher
                        .requestMatchers(GET, "/teacher/students").hasAuthority(Role.TEACHER.toString())
                        .requestMatchers(GET, "/teacher/internshipEvaluations").hasAnyAuthority(Role.TEACHER.toString())
                        .requestMatchers(POST, "/teacher/evaluations/*").hasAnyAuthority(Role.TEACHER.toString())
                        // GS
                        .requestMatchers(GET, "/jobOffers/waitingForApproval").hasAuthority(Role.PROGRAM_MANAGER.toString()) // TODO : Ne peux pas être paginé
                        .requestMatchers(POST, "/internshipManager/validateInternship/*").hasAuthority(Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(GET, "/intershipmanager/validateCV/waitingforapproval").hasAnyAuthority(Role.PROGRAM_MANAGER.toString()) // TODO : Ajouter pagination + filtre
                        .requestMatchers(GET, "/intershipmanager/validateCV/discipline/*").hasAuthority(Role.PROGRAM_MANAGER.toString()) // TODO : Merge?
                        .requestMatchers(PUT, "/intershipmanager/validateCV/*").hasAuthority(Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(POST,"/internshipManager/contracts/begin/*").hasAuthority(Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(GET, "/internshipManager/profs/*").hasAuthority(Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(GET, "/internshipManager/profs/*/currentInterns").hasAuthority(Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(GET, "/internshipManager/interns/*").hasAuthority(Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(POST, "/internshipManager/interns/assignToProf").hasAuthority(Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(GET, "/internshipManager/validatedOffers").hasAuthority(Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(GET, "/internshipManager/signedUpStudents").hasAuthority(Role.PROGRAM_MANAGER.toString())

                        // GS reports
                        .requestMatchers(GET, "/internshipManager/nonValidatedOffers").hasAuthority(Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(GET, "/internshipManager/validatedOffers").hasAuthority(Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(GET, "/internshipManager/signedUpStudents").hasAuthority(Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(GET, "/internshipManager/studentsNoCV").hasAuthority(Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(GET, "/internshipManager/studentsCVNotValidated").hasAuthority(Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(GET, "/internshipManager/studentsNoInterview").hasAuthority(Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(GET, "/internshipManager/studentsAwaitingInterview").hasAuthority(Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(GET, "/internshipManager/studentsAwaitingInterviewResponse").hasAuthority(Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(GET, "/internshipManager/studentsWhoFoundInternship").hasAuthority(Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(GET, "/internshipManager/studentsNotEvaluatedBySupervisor").hasAuthority(Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(GET, "/internshipManager/studentsSupervisorHasntEvaluatedEnterprise").hasAuthority(Role.PROGRAM_MANAGER.toString())


                        //Shared
                        .requestMatchers(GET, "/cvs/*").hasAnyAuthority(Role.PROGRAM_MANAGER.toString(),Role.STUDENT.toString())
                        .requestMatchers(GET, "/jobOffers/*").hasAnyAuthority(Role.PROGRAM_MANAGER.toString(), Role.STUDENT.toString())
                        .requestMatchers(GET, "/jobInterview/*").hasAnyAuthority(Role.EMPLOYEUR.toString(), Role.STUDENT.toString())
                        .requestMatchers(GET, "/internship/*").hasAnyAuthority(Role.EMPLOYEUR.toString(), Role.STUDENT.toString())
                        .requestMatchers(GET, "/students/disciplines/*").hasAnyAuthority(Role.PROGRAM_MANAGER.toString(), Role.TEACHER.toString()) // Contient pagination + filtre
                        .requestMatchers(GET,"/internshipOffers/*").hasAnyAuthority(Role.STUDENT.toString(),Role.EMPLOYEUR.toString(),Role.PROGRAM_MANAGER.toString()) // Contient pagination
                        .requestMatchers(GET,"/contracts/").hasAnyAuthority(Role.STUDENT.toString(),Role.EMPLOYEUR.toString(),Role.PROGRAM_MANAGER.toString()) // Contient pagination
                        .requestMatchers(POST,"/contracts/sign/*").hasAnyAuthority(Role.STUDENT.toString(),Role.EMPLOYEUR.toString(),Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(POST, "/employer/evaluations/*").hasAnyAuthority(Role.EMPLOYEUR.toString(), Role.PROGRAM_MANAGER.toString(), Role.TEACHER.toString())
                        .requestMatchers(GET, "/print/contracts/*").hasAnyAuthority(Role.STUDENT.toString(),Role.EMPLOYEUR.toString(),Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(GET, "/print/evaluationIntern/*").hasAnyAuthority(Role.EMPLOYEUR.toString(),Role.TEACHER.toString(),Role.PROGRAM_MANAGER.toString())
                        .requestMatchers(GET, "/print/evaluationEmployer/*").hasAnyAuthority(Role.TEACHER.toString(),Role.PROGRAM_MANAGER.toString())



                        /*.requestMatchers(GET, "/user/*").hasAnyAuthority("EMPRUNTEUR", "PREPOSE", "GESTIONNAIRE")
                        .requestMatchers("/emprunteur/**").hasAuthority("EMPRUNTEUR")
                        .requestMatchers("/prepose/**").hasAuthority("PREPOSE")
                        .requestMatchers("/gestionnaire/**").hasAuthority("GESTIONNAIRE")*/
                        .anyRequest().denyAll()
                )
                .headers(headers -> headers.frameOptions(Customizer.withDefaults()).disable()) // for h2-console
                .sessionManagement((secuManagement) -> {
                    secuManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)  //TODO : Insert Project Manager
                .exceptionHandling(configurer -> configurer.authenticationEntryPoint(authenticationEntryPoint))
        ;

        return http.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
        return new JwtAuthenticationFilter(jwtTokenProvider, userAppRepository);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }
    //Empeche le brute force
    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}