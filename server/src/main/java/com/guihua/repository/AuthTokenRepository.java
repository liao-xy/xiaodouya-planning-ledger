package com.guihua.repository;

import com.guihua.domain.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthTokenRepository extends JpaRepository<AuthToken, String> { void deleteByUserId(Long userId); }
