package com.banking.cif.controller;

import com.banking.cif.dto.CustomerDTO;
import com.banking.cif.dto.CustomerRequest;
import com.banking.cif.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")

public class CustomerController {

     @org.springframework.beans.factory.annotation.Autowired
    private CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody CustomerRequest request) {
        CustomerDTO created = customerService.createCustomer(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        List<CustomerDTO> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomer(@PathVariable Integer id, @RequestHeader(value="Authorization-User-Id", required=false) Integer authUserId) {
        // Simple simulated IDOR protection - in a real system this would use Spring Security
        if (authUserId != null && !authUserId.equals(id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403
        }
        
        CustomerDTO customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(customer);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CustomerDTO>> searchCustomers(@RequestParam String name) {
        List<CustomerDTO> customers = customerService.searchCustomersByName(name);
        return ResponseEntity.ok(customers);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(@PathVariable Integer id, @Valid @RequestBody CustomerRequest request) {
        CustomerDTO updated = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(updated);
    }
}
