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
  NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다"),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
  CURRENT_PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다"),
  SAME_AS_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "새 비밀번호는 현재 비밀번호와 달라야 합니다"),
  SAME_AS_CURRENT_NICKNAME(HttpStatus.BAD_REQUEST, "기존 닉네임과 동일합니다"),
  DUPLICATED_BRAND_NAME(HttpStatus.CONFLICT,"이미 존재하는 브랜드 이름입니다"),
  BRAND_NOT_FOUND(HttpStatus.NOT_FOUND, "브랜드를 찾을 수 없습니다");

  private final HttpStatus status;
  private final String message;
}
