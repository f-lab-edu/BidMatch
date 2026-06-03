package com.project.bidmatch.auth.dto;

import com.project.bidmatch.domain.user.User;

public record SignupResponse(
    Long userId,
    String email,
    String nickname
) {
  public static SignupResponse from(User user) {
    return new SignupResponse(user.getId(), user.getEmail(), user.getNickname());
  }
}
