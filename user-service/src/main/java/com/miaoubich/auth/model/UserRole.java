package com.miaoubich.auth.model;

public enum UserRole {

	// Realm roles
	ADMIN("admin", RoleScope.REALM), AUDIT("audit", RoleScope.REALM), COMPLIANCE("compliance", RoleScope.REALM),
	FRAUD("fraud", RoleScope.REALM), SUPPORT("support", RoleScope.REALM), USER("user", RoleScope.REALM),

	// Specific user roles
	SUPER("super"), ADMIN1("admin1"), AUDIT1("audit1"), AUDIT2("audit2"), COMPLIANCE1("compliance1"),
	COMPLIANCE2("compliance2"), FRAUD1("fraud1"), FRAUD2("fraud2"), SUPPORT1("support1"), SUPPORT2("support2"),
	USER1("user1"), USER2("user2"), USER3("user3");

	public enum RoleScope {
		REALM, CLIENT
	}

	private final String keycloakRoleName;
	private final RoleScope scope;

	UserRole(String keycloakRoleName) {
		this(keycloakRoleName, RoleScope.CLIENT);
	}

	UserRole(String keycloakRoleName, RoleScope scope) {
		this.keycloakRoleName = keycloakRoleName;
		this.scope = scope;
	}

	public String getKeycloakRoleName() {
		return keycloakRoleName;
	}

	public RoleScope getScope() {
		return scope;
	}

	public boolean isRealmRole() {
		return scope == RoleScope.REALM;
	}

	public boolean isClientRole() {
		return scope == RoleScope.CLIENT;
	}
}
