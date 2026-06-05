package com.project.bidmatch.controller;

import com.project.bidmatch.dto.NicknameUpdateRequest;
import com.project.bidmatch.dto.PasswordChangeRequest;
import com.project.bidmatch.dto.UserResponse;
import com.project.bidmatch.service.CustomUserPrincipal;
import com.project.bidmatch.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/me")
  public UserResponse getMyInfo(@AuthenticationPrincipal CustomUserPrincipal principal) {
    return userService.getMyInfo(principal.getId());
  }

  @PatchMapping("/me/nickname")
  public UserResponse updateNickname(
      @AuthenticationPrincipal CustomUserPrincipal principal,
      @Validated @RequestBody NicknameUpdateRequest request
  ) {
    return userService.updateNickname(principal.getId(), request);
  }

  @PatchMapping("/me/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void changePassword(
      @AuthenticationPrincipal CustomUserPrincipal principal,
      @Validated @RequestBody PasswordChangeRequest request
  ) {
    userService.changePassword(principal.getId(), request);
  }
}
