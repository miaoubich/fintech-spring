package com.miaoubich.customerservice.controller;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.miaoubich.customerservice.dto.CustomerDto;
import com.miaoubich.customerservice.dto.CustomerStatusUpdateDto;
import com.miaoubich.customerservice.model.CustomerStatus;
import com.miaoubich.customerservice.service.CustomerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/customers")
public class CustomerController {

	private final CustomerService customerService;

	public CustomerController(CustomerService customerService) {
		this.customerService = customerService;
	}

	// Authority: CUSTOMER_WRITE
	// Roles: admin, admin1, super
	@PostMapping
	public ResponseEntity<CustomerDto> createCustomer(@Valid @RequestBody CustomerDto customerDto) {
		CustomerDto createdCustomer = customerService.createCustomer(customerDto);
		
		URI location = ServletUriComponentsBuilder
						.fromCurrentRequest()
						.path("/{id}")
						.buildAndExpand(createdCustomer.id())
						.toUri();
		
		return ResponseEntity.created(location).body(createdCustomer);
	}

	// Authority: CUSTOMER_READ
	@GetMapping("/{id}")
	public ResponseEntity<CustomerDto> getCustomerById(@PathVariable Long id) {
		return ResponseEntity.ok(customerService.getCustomerById(id));
	}

	// Authority: CUSTOMER_READ
	// Roles: user, user1-3, support, support1-2, fraud, fraud1, audit, compliance,
	// compliance1-2, admin, admin1, super
	@GetMapping
	public ResponseEntity<List<CustomerDto>> getAllCustomers() {
		return ResponseEntity.ok(customerService.getAllCustomers());
	}

	// Authority: CUSTOMER_READ (Paginated + N+1 safe via @BatchSize)
	@GetMapping("/page")
	public ResponseEntity<Page<CustomerDto>> getCustomersPage(Pageable pageable) {
		return ResponseEntity.ok(customerService.getCustomersPage(pageable));
	}

	// Authority: CUSTOMER_FULL (Heavy Hibernate Batch Insert)
	// Roles: super
	@PostMapping("/admin/batch")
	public ResponseEntity<List<CustomerDto>> batchCreateCustomers(@Valid @RequestBody List<CustomerDto> dtos) {
		List<CustomerDto> customers = customerService.batchCreateCustomers(dtos);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(customers);
	}

	// Authority: CUSTOMER_WRITE
	// Roles: admin, admin1, super
	@PutMapping("/{id}")
	public ResponseEntity<CustomerDto> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerDto dto) {
		return ResponseEntity.ok(customerService.updateCustomer(id, dto));
	}

	// Authority: CUSTOMER_DELETE
	// Roles: admin, admin1, super
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteCustomer(@PathVariable Long id) {
		customerService.deleteCustomer(id);
	}

	// Authority: CUSTOMER_AUDIT
	// Roles: audit, compliance, compliance1, compliance2, super
	@GetMapping("/audit/status/{status}")
	public ResponseEntity<List<CustomerDto>> auditCustomersByStatus(@PathVariable CustomerStatus status) {
		return ResponseEntity.ok(customerService.getCustomersByStatusForAudit(status));
	}

	// Authority: CUSTOMER_SUSPEND
	// Roles: fraud, fraud1, super
	@PatchMapping("/{id}/suspend")
	public ResponseEntity<CustomerDto> suspendCustomer(@PathVariable Long id) {
		CustomerDto suspendedCustomer = customerService.updateStatusOrNotes(id,
				new CustomerStatusUpdateDto(CustomerStatus.SUSPENDED, "Suspended by Fraud team"));
	
		return ResponseEntity.ok(suspendedCustomer);
	}

	// Authority: CUSTOMER_SUPPORT
	// Roles: support, support1, support2, super
	@PatchMapping("/{id}/support")
	public ResponseEntity<CustomerDto> updateSupportNotes(@PathVariable Long id, @Valid @RequestBody CustomerStatusUpdateDto dto) {
		return ResponseEntity.ok(customerService.updateStatusOrNotes(id, dto));
	}
}