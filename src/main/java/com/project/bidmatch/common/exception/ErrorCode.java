package com.project.bidmatch.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 요청입니다"),
  USER_SUSPENDED(HttpStatus.FORBIDDEN, "비활성화된 계정입니다"),
  EMAIL_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다"),
  NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다");

  private final HttpStatus status;
  private final String message;
}
