package com.miaoubich.auth.model;

public enum UserRole {

	// Realm rolesADMIN("admin"),
    AUDIT("audit"),
    COMPLIANCE("compliance"),
    FRAUD("fraud"),
    SUPPORT("support"),
    USER("user"),
    
    // Specific user roles
    SUPER("super"),
    ADMIN1("admin1"),
    COMPLIANCE1("compliance1"),
    COMPLIANCE2("compliance2"),
    FRAUD1("fraud1"),
    SUPPORT1("support1"),
    SUPPORT2("support2"),
    USER1("user1"),
    USER2("user2"),
    USER3("user3");
	
	private final String keycloakRoleName;
	
	UserRole(String	keycloakRoleName){
		this.keycloakRoleName = keycloakRoleName;
	}
	
	public String getKeycloakRoleName() {
		return keycloakRoleName;
	}
}
