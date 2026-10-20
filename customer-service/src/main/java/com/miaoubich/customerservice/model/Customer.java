package com.miaoubich.customerservice.model;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_seq")
	@SequenceGenerator(name = "customer_seq", sequenceName = "customer_id_seq", allocationSize = 50)
	private Long id;

	@Column(nullable = false)
	private String firstName;

	@Column(nullable = false)
	private String lastName;

	@Column(nullable = false, unique = true)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CustomerStatus status = CustomerStatus.ACTIVE;

	@Column(length = 1000)
	private String supportNotes;

	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@BatchSize(size = 50) // Mitigates N+1 when paginating
	private List<Address> addresses = new ArrayList<>();

	// Bidirectional helper methods (crucial for @OneToMany / @ManyToOne)
	public void addAddress(Address address) {
		addresses.add(address);
		address.setCustomer(this);
	}

	public void removeAddress(Address address) {
		addresses.remove(address);
		address.setCustomer(null);
	}

	public void clearAddresses() {
		for (Address address : new ArrayList<>(addresses)) {
			removeAddress(address);
		}
		//or a stream version
		// new ArrayList<>(addresses).forEach(this::removeAddress);
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public CustomerStatus getStatus() {
		return status;
	}

	public void setStatus(CustomerStatus status) {
		this.status = status;
	}

	public String getSupportNotes() {
		return supportNotes;
	}

	public void setSupportNotes(String supportNotes) {
		this.supportNotes = supportNotes;
	}

	public List<Address> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<Address> addresses) {
		this.addresses = addresses;
	}
}