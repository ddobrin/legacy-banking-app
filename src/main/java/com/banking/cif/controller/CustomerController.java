package com.banking.cif.controller;

import com.banking.cif.dto.CreateCustomerRequest;
import com.banking.cif.dto.CustomerDto;
import com.banking.cif.dto.UpdateCustomerRequest;
import com.banking.cif.service.CustomerService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for managing customer CIF records. */
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

  private final CustomerService customerService;

  /**
   * Creates a new {@link CustomerController}.
   *
   * @param customerService customer service
   */
  public CustomerController(CustomerService customerService) {
    this.customerService = customerService;
  }

  /**
   * Lists all customers with their account counts.
   *
   * @return list of customers
   */
  @GetMapping
  public List<CustomerDto> getAllCustomers() {
    return customerService.getAllCustomers();
  }

  /**
   * Searches customers by first or last name fragment.
   *
   * @param name optional name search query
   * @return matching customers
   */
  @GetMapping("/search")
  public List<CustomerDto> searchCustomers(
      @RequestParam(name = "name", required = false) String name) {
    return customerService.searchCustomersByName(name);
  }

  /**
   * Looks up a customer by numeric ID or name fragment, including associated accounts.
   *
   * @param idOrName numeric customer ID or name
   * @return customer details
   */
  @GetMapping("/{idOrName}")
  public CustomerDto getCustomer(@PathVariable("idOrName") String idOrName) {
    return customerService.getCustomerByIdOrName(idOrName);
  }

  /**
   * Registers a new customer CIF profile.
   *
   * @param request customer creation payload
   * @return HTTP 201 with created customer
   */
  @PostMapping
  public ResponseEntity<CustomerDto> createCustomer(
      @Valid @RequestBody CreateCustomerRequest request) {
    CustomerDto created = customerService.createCustomer(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  /**
   * Updates an existing customer profile.
   *
   * @param id customer ID
   * @param request update payload
   * @return updated customer details
   */
  @PutMapping("/{id}")
  public CustomerDto updateCustomer(
      @PathVariable("id") Integer id, @Valid @RequestBody UpdateCustomerRequest request) {
    return customerService.updateCustomer(id, request);
  }

  /**
   * Deletes a customer record that has no associated accounts.
   *
   * @param id customer ID
   * @return HTTP 204 No Content
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCustomer(@PathVariable("id") Integer id) {
    customerService.deleteCustomer(id);
    return ResponseEntity.noContent().build();
  }
}
