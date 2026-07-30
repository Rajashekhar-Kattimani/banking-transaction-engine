package com.bank.auth.token.entity;

import java.time.Instant;

import com.bank.auth.user.entity.User;
import com.bank.persistence.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
public class RefreshToken extends BaseEntity {

	@Column(nullable = false, unique = true, length = 512)
	private String token;

	@Column(nullable = false)
	private Instant expiryDate;

	@Column(nullable = false)
	private boolean revoked;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;
}
