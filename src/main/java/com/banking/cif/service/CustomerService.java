package com.banking.cif.service;

import com.banking.cif.dto.CustomerDTO;
import com.banking.cif.dto.CustomerRequest;
import com.banking.cif.exception.ConflictException;
import com.banking.cif.exception.ResourceNotFoundException;
import com.banking.cif.model.Customer;
import com.banking.cif.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service

public class CustomerService {
     @org.springframework.beans.factory.annotation.Autowired
    private CustomerRepository customerRepository;
    private SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public CustomerDTO createCustomer(CustomerRequest request) {
        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Email is already registered.");
        }

        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setDateOfBirth(request.getDateOfBirth());
        
        // Securely generate CIF Number
        customer.setCifNumber(generateCifNumber());

        Customer savedCustomer = customerRepository.save(customer);
        return mapToDTO(savedCustomer);
    }

    public CustomerDTO getCustomerById(Integer id) {
        return customerRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<CustomerDTO> searchCustomersByName(String name) {
        return customerRepository.searchByName(name).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomerDTO updateCustomer(Integer id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        if (!customer.getEmail().equals(request.getEmail()) && 
            customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Email is already registered.");
        }

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setDateOfBirth(request.getDateOfBirth());

        return mapToDTO(customerRepository.save(customer));
    }

    private String generateCifNumber() {
        return "CIF" + (100000000L + (long)(secureRandom.nextDouble() * 900000000L));
    }

    private CustomerDTO mapToDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setCustomerId(customer.getCustomerId());
        dto.setCifNumber(customer.getCifNumber());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setEmail(customer.getEmail());
        dto.setDateOfBirth(customer.getDateOfBirth());
        dto.setKycStatus(customer.getKycStatus());
        return dto;
    }
}
