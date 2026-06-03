package com.project.bidmatch.auth.controller;

import com.project.bidmatch.auth.dto.SignupRequest;
import com.project.bidmatch.auth.dto.SignupResponse;
import com.project.bidmatch.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/signup")
  @ResponseStatus(HttpStatus.CREATED)
  public SignupResponse signup(@Validated @RequestBody SignupRequest signupRequest) {
    return authService.signup(signupRequest);
  }
}
