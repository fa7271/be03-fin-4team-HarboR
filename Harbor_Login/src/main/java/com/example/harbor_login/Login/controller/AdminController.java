package com.example.harbor_login.Login.controller;

import com.example.harbor_login.Login.domain.Login;
import com.example.harbor_login.Login.service.EmailService;
import com.example.harbor_login.Login.service.LoginService;
import com.example.harbor_login.global.common.CommonResponse;
import com.example.harbor_login.global.config.CustomUserDetails;
import com.example.harbor_login.global.config.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/login/admin")
@RequiredArgsConstructor
public class AdminController {

    private final LoginService loginService;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/active/{email}")
    public ResponseEntity<CommonResponse> mailConfirm(@PathVariable(value = "email") String email, @AuthenticationPrincipal CustomUserDetails userDetails) {
        emailService.sendEmail(email);
        loginService.findByEmail(email);
        return new ResponseEntity<>(new CommonResponse("Employee number transmitted successfully", email), HttpStatus.OK);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/employeelist")
    public ResponseEntity<CommonResponse> employeeList(Pageable pageable) {
        return new ResponseEntity<>(new CommonResponse<>("Employee list retrieved successfully", loginService.findAll(pageable)), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{email}")
    public ResponseEntity<CommonResponse> deleteUser(@PathVariable(value = "email") String email) {

        return new ResponseEntity<>(new CommonResponse<>("Employee delete successfully", loginService.delete(email)), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getcountallemployee")
    public ResponseEntity<CommonResponse> getcountallemployee(Pageable pageable) {
        return new ResponseEntity<>(new CommonResponse<>("get all employee count", loginService.getcountallemployee(pageable)), HttpStatus.OK);
    }
}
