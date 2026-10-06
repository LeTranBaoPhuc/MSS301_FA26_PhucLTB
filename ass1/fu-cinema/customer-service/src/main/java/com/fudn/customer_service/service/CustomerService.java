package com.fudn.customer_service.service;

import com.fudn.customer_service.dto.*;
import com.fudn.customer_service.exception.ApiException;
import com.fudn.customer_service.model.Customer;
import com.fudn.customer_service.model.CustomerStatus;
import com.fudn.customer_service.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    // ===================== CUSTOMER (F2) =====================

    // TODO 2.4
    @Transactional
    public CustomerResponse register(RegisterRequest request) {
        ensureEmailAvailable(request.email(), null);
        Customer customer = new Customer();
        customer.setCustomerName(request.customerName());
        customer.setTelephone(request.telephone());
        customer.setEmail(request.email());
        customer.setCustomerBirthday(request.customerBirthday());
        customer.setCustomerStatus(CustomerStatus.ACTIVE);
        customer.setPassword(passwordEncoder.encode(request.password()));
        Customer saved = customerRepository.save(customer);
        log.info("Customer registered: id={}, email={}", saved.getCustomerId(), saved.getEmail());
        return CustomerResponse.from(saved);
    }

    // TODO 2.5
    public CustomerResponse getProfile(Long customerId) {
        return CustomerResponse.from(findCustomer(customerId));
    }

    @Transactional
    public CustomerResponse updateProfile(Long customerId, ProfileUpdateRequest request) {
        Customer customer = findCustomer(customerId);
        customer.setCustomerName(request.customerName());
        customer.setTelephone(request.telephone());
        customer.setCustomerBirthday(request.customerBirthday());
        return CustomerResponse.from(customerRepository.save(customer));
    }

    @Transactional
    public void changePassword(Long customerId, ChangePasswordRequest request) {
        Customer customer = findCustomer(customerId);
        if (!passwordEncoder.matches(request.oldPassword(), customer.getPassword())) {
            throw ApiException.badRequest("Old password is incorrect");
        }
        if (request.oldPassword().equals(request.newPassword())) {
            throw ApiException.badRequest("New password must be different from the old password");
        }
        customer.setPassword(passwordEncoder.encode(request.newPassword()));
        customerRepository.save(customer);
    }

    // ===================== ADMIN (F3) =====================

    // TODO 3.2
    public List<CustomerResponse> getAllCustomers(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return customerRepository.findByCustomerNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByCustomerIdAsc(keyword, keyword)
                    .stream().map(CustomerResponse::from).toList();
        }
        return customerRepository.findAll(Sort.by("customerId")).stream().map(CustomerResponse::from).toList();
    }

    public CustomerResponse getCustomerById(Long id) {
        return CustomerResponse.from(findCustomer(id));
    }

    @Transactional
    public CustomerResponse createCustomer(AdminCustomerRequest request) {
        ensureEmailAvailable(request.email(), null);
        if (request.password() == null || request.password().isBlank()) {
            throw ApiException.badRequest("Password is required when creating customer");
        }
        Customer customer = new Customer();
        customer.setCustomerName(request.customerName());
        customer.setTelephone(request.telephone());
        customer.setEmail(request.email());
        customer.setCustomerBirthday(request.customerBirthday());
        customer.setCustomerStatus(request.customerStatus());
        customer.setPassword(passwordEncoder.encode(request.password()));
        return CustomerResponse.from(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, AdminCustomerRequest request) {
        ensureEmailAvailable(request.email(), id);
        Customer customer = findCustomer(id);
        customer.setCustomerName(request.customerName());
        customer.setTelephone(request.telephone());
        customer.setEmail(request.email());
        customer.setCustomerBirthday(request.customerBirthday());
        customer.setCustomerStatus(request.customerStatus());
        if (request.password() != null && !request.password().isBlank()) {
            customer.setPassword(passwordEncoder.encode(request.password()));
        }
        return CustomerResponse.from(customerRepository.save(customer));
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = findCustomer(id);
        customer.setCustomerStatus(CustomerStatus.INACTIVE);
        customerRepository.save(customer);
    }

    // ===================== HELPER =====================

    private void ensureEmailAvailable(String email, Long excludeId) {
        boolean exists = excludeId == null
                ? customerRepository.existsByEmailIgnoreCase(email)
                : customerRepository.existsByEmailIgnoreCaseAndCustomerIdNot(email, excludeId);
        if (exists || adminEmail.equalsIgnoreCase(email)) {
            throw ApiException.conflict("Email is already in use: " + email);
        }
    }

    private Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Customer not found with id: " + id));
    }
}
