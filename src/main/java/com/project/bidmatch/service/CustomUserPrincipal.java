package com.project.bidmatch.service;

import java.util.Collection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@RequiredArgsConstructor
public class CustomUserPrincipal implements UserDetails {

  private final Long id;
  private final String email;
  private final String password;
  private final Collection<? extends GrantedAuthority> authorities;

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  // Spring Security는 비밀번호 검사 전에 isEnabled()를 보는데 false면 DisabledException으로 막아버림
  // return isActive; 를 할 경우 SUSPENDED 상태인 유저는 로그인 자체가 불가능 하게 됨.
  public boolean isEnabled() {
    return true;
  }
}
