package com.banking.cif.service;

import com.banking.cif.dto.AccountDTO;
import com.banking.cif.dto.AccountRequest;
import com.banking.cif.dto.AccountStatusRequest;
import com.banking.cif.exception.InvalidRequestException;
import com.banking.cif.exception.ResourceNotFoundException;
import com.banking.cif.model.Account;
import com.banking.cif.model.Customer;
import com.banking.cif.model.Product;
import com.banking.cif.repository.AccountRepository;
import com.banking.cif.repository.CustomerRepository;
import com.banking.cif.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service

public class AccountService {
     @org.springframework.beans.factory.annotation.Autowired
    private AccountRepository accountRepository;
     @org.springframework.beans.factory.annotation.Autowired
    private CustomerRepository customerRepository;
     @org.springframework.beans.factory.annotation.Autowired
    private ProductRepository productRepository;
    private SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public AccountDTO createAccount(AccountRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Product product = productRepository.findById(request.getProductCode())
                .orElseThrow(() -> new InvalidRequestException("Invalid product code"));

        Account account = new Account();
        account.setCustomer(customer);
        account.setProduct(product);
        account.setBalance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO);
        account.setStatus("ACTIVE");
        account.setAccountNumber(generateAccountNumber());

        Account savedAccount = accountRepository.save(account);
        return mapToDTO(savedAccount);
    }

    public AccountDTO getAccountById(Integer id) {
        return accountRepository.findById(id)
                .map(this::mapToDTO)
                .orElseGet(() -> accountRepository.findByAccountNumber(id)
                        .map(this::mapToDTO)
                        .orElseThrow(() -> new ResourceNotFoundException("Account not found with id or account number: " + id)));
    }

    @Transactional
    public AccountDTO updateAccountStatus(Integer id, AccountStatusRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));

        account.setStatus(request.getStatus());
        if ("CLOSED".equalsIgnoreCase(request.getStatus())) {
            account.setClosedAt(LocalDateTime.now());
        }

        return mapToDTO(accountRepository.save(account));
    }

    private Integer generateAccountNumber() {
        int accNum;
        do {
            accNum = 100000000 + secureRandom.nextInt(900000000);
        } while (accountRepository.existsByAccountNumber(accNum));
        return accNum;
    }

    private AccountDTO mapToDTO(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setAccountId(account.getAccountId());
        dto.setCustomerId(account.getCustomer().getCustomerId());
        dto.setProductCode(account.getProduct().getProductCode());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setBalance(account.getBalance());
        dto.setStatus(account.getStatus());
        return dto;
    }
}
