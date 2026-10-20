package com.miaoubich.customerservice.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.miaoubich.customerservice.dto.AddressDto;
import com.miaoubich.customerservice.dto.CustomerDto;
import com.miaoubich.customerservice.dto.CustomerStatusUpdateDto;
import com.miaoubich.customerservice.model.Address;
import com.miaoubich.customerservice.model.Customer;
import com.miaoubich.customerservice.model.CustomerStatus;
import com.miaoubich.customerservice.repository.CustomerRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
@Transactional(readOnly = true)
public class CustomerService {

    private static final int BATCH_SIZE = 50;

    private final CustomerRepository customerRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // 1. N+1 Safe List Read (1 SQL Query via JOIN FETCH)
    public List<CustomerDto> getAllCustomers() {
        return customerRepository.findAllWithAddresses().stream()
                .map(this::toDto)
                .toList();
    }

    // 2. N+1 Safe Paginated Read (2 SQL Queries total regardless of page size, via @BatchSize)
    public Page<CustomerDto> getCustomersPage(Pageable pageable) {
        return customerRepository.findAll(pageable).map(this::toDto);
    }

    // 3. N+1 Safe Single Read (1 SQL Query via JOIN FETCH)
    public CustomerDto getCustomerById(Long id) {
        Customer customer = customerRepository.findByIdWithAddresses(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        return toDto(customer);
    }

    // 4. N+1 Safe Audit Read by Status (1 SQL Query via @EntityGraph)
    public List<CustomerDto> getCustomersByStatusForAudit(CustomerStatus status) {
        return customerRepository.findByStatus(status).stream()
                .map(this::toDto)
                .toList();
    }

    // 5. Standard Create
    @Transactional
    public CustomerDto createCustomer(CustomerDto dto) {
        Customer customer = toEntity(dto);
        Customer saved = customerRepository.save(customer);
        return toDto(saved);
    }

    // 6. Heavy Hibernate Batch Insert (Chunked flush & clear)
    @Transactional
    public List<CustomerDto> batchCreateCustomers(List<CustomerDto> dtos) {
        List<CustomerDto> savedDtos = new ArrayList<>(dtos.size());

        for (int i = 0; i < dtos.size(); i++) {
            Customer customer = toEntity(dtos.get(i));
            entityManager.persist(customer);
            savedDtos.add(toDto(customer));

            if ((i + 1) % BATCH_SIZE == 0) {
                // Pushes 50 customers + their addresses in batched SQL statements
                // and frees PersistenceContext memory
                entityManager.flush();
                entityManager.clear();
            }
        }

        entityManager.flush();
        entityManager.clear();
        return savedDtos;
    }

    // 7. Update Customer and replace Addresses (uses orphanRemoval = true)
    @Transactional
    public CustomerDto updateCustomer(Long id, CustomerDto dto) {
        Customer customer = customerRepository.findByIdWithAddresses(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        customer.setFirstName(dto.firstName());
        customer.setLastName(dto.lastName());
        customer.setEmail(dto.email());

        customer.clearAddresses();
        for (AddressDto addrDto : dto.addresses()) {
            customer.addAddress(toAddressEntity(addrDto));
        }

        return toDto(customer);
    }

    // 8. Fraud / Support Status Update
    @Transactional
    public CustomerDto updateStatusOrNotes(Long id, CustomerStatusUpdateDto updateDto) {
        Customer customer = customerRepository.findByIdWithAddresses(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        customer.setStatus(updateDto.status());
        if (updateDto.supportNotes() != null) {
            customer.setSupportNotes(updateDto.supportNotes());
        }
        return toDto(customer);
    }

    // 9. Delete Customer (Cascades to Addresses)
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findByIdWithAddresses(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        customerRepository.delete(customer);
    }

    // --- Mappers ---
    private Customer toEntity(CustomerDto customerDto) {
        Customer customer = new Customer();
        customer.setFirstName(customerDto.firstName());
        customer.setLastName(customerDto.lastName());
        customer.setEmail(customerDto.email());
        if (customerDto.status() != null) {
            customer.setStatus(customerDto.status());
        }
        customer.setSupportNotes(customerDto.supportNotes());

        if (customerDto.addresses() != null) {
            for (AddressDto addrDto : customerDto.addresses()) {
                customer.addAddress(toAddressEntity(addrDto));
            }
        }
        return customer;
    }

    private Address toAddressEntity(AddressDto dto) {
        Address address = new Address();
        address.setStreet(dto.street());
        address.setCity(dto.city());
        address.setState(dto.state());
        address.setZipCode(dto.zipCode());
        address.setCountry(dto.country());
        return address;
    }

    private CustomerDto toDto(Customer customer) {
        List<AddressDto> addressDtos = customer.getAddresses().stream()
                .map(a -> new AddressDto(
                        a.getId(), a.getStreet(), a.getCity(),
                        a.getState(), a.getZipCode(), a.getCountry()
                ))
                .toList();

        return new CustomerDto(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getStatus(),
                customer.getSupportNotes(),
                addressDtos
        );
    }
}