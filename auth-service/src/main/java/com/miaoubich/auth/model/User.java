package com.miaoubich.auth.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.Id;

import jakarta.persistence.*;

@Entity
@Table(name = "users", uniqueConstraints = { @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
		@UniqueConstraint(name = "uk_users_email", columnNames = "email"),
		@UniqueConstraint(name = "uk_users_keycloak_id", columnNames = "keycloak_id") }, indexes = {
				// Critical for the 1-minute scheduler query: findBySyncedFalse()
				@Index(name = "idx_users_synced", columnList = "synced"),
				@Index(name = "idx_users_username", columnList = "username") })
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
	@SequenceGenerator(name = "user_seq", sequenceName = "user_id_seq", allocationSize = 50)
	private Long id;

	@Column(nullable = false, length = 100)
	private String username;

	@Column(nullable = false, length = 150)
	private String email;

	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;

	@Column(name = "last_name", nullable = false, length = 100)
	private String lastName;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	// Temporarily holds password until synced with Keycloak, then wiped to null
	@Column(name = "pending_keycloak_password")
	private String pendingKeycloakPassword;

	@Column(name = "keycloak_id", length = 64)
	private String keycloakId;
	/*
	 * Optimistic locking: prevents race conditions between the 1-minute sync job
	 * and HTTP requests
	 */
	@Version
	private Long version;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_roles_user")), indexes = @Index(name = "idx_user_roles_user_id", columnList = "user_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 50)
	@BatchSize(size = 50) // Prevents N+1 when findBySyncedFalse() loads multiple users and their roles!
	private Set<UserRole> roles = new HashSet<>();

	@Column(nullable = false)
	private boolean synced = false;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "last_synced_at")
	private Instant lastSyncedAt;

	// --- JPA Best Practice equals & hashCode ---
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof User other))
			return false;
		return id != null && id.equals(other.getId());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getKeycloakId() {
		return keycloakId;
	}

	public void setKeycloakId(String keycloakId) {
		this.keycloakId = keycloakId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getPendingKeycloakPassword() {
		return pendingKeycloakPassword;
	}

	public void setPendingKeycloakPassword(String pendingKeycloakPassword) {
		this.pendingKeycloakPassword = pendingKeycloakPassword;
	}

	public Set<UserRole> getRoles() {
		return roles;
	}

	public void setRoles(Set<UserRole> roles) {
		this.roles = roles;
	}

	public boolean isSynced() {
		return synced;
	}

	public void setSynced(boolean synced) {
		this.synced = synced;
	}

	public Instant getLastSyncedAt() {
		return lastSyncedAt;
	}

	public void setLastSyncedAt(Instant lastSyncedAt) {
		this.lastSyncedAt = lastSyncedAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

}
