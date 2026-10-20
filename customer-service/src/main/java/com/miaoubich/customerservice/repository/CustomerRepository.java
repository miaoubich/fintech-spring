package com.miaoubich.customerservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.miaoubich.customerservice.model.Customer;
import com.miaoubich.customerservice.model.CustomerStatus;

public interface CustomerRepository extends JpaRepository<Customer, Long>{

	// 1. Single customer + addresses in 1 query (N+1 safe)
	@Query("SELECT DISTINCT c FROM Customer c LEFT JOIN FETCH c.addresses WHERE c.id = :id")
	Optional<Customer> findByIdWithAddresses(@Param("id") Long id);
	
	// 2. All customers + addresses in 1 query (N+1 safe)
	@Query("SELECT DISTINCT c FROM Customer c LEFT JOIN FETCH c.addresses")
	List<Customer> findAllWithAddresses();
	
	// 3. Find by status + addresses in 1 query using @EntityGraph (N+1 safe)
	/* Spring Data JPA internally transforms it into
	 * SELECT c.*, a.*
		FROM customers c
		LEFT JOIN addresses a ON a.customer_id = c.id
		WHERE c.status = ?
	 * */
    @EntityGraph(attributePaths = {"addresses"})
    List<Customer> findByStatus(CustomerStatus status);
	
	// Technique 3: Paginated query + @BatchSize(size = 50)
    // Note: Do NOT use JOIN FETCH with Pageable (Hibernate will load the whole table in memory).
    // Instead, query the page of Customers, and @BatchSize on Customer.addresses will fetch
    // all addresses for the page in a single secondary WHERE customer_id IN (...) query!
    Page<Customer> findAll(Pageable pageable);
}
