package com.fudn.customer_service.dto;

import com.fudn.customer_service.model.Customer;
import com.fudn.customer_service.model.CustomerStatus;

import java.time.LocalDate;

public record CustomerResponse(
        Long customerId,
        String customerName,
        String telephone,
        String email,
        LocalDate customerBirthday,
        CustomerStatus customerStatus) {

    public static CustomerResponse from(Customer c) {
        return new CustomerResponse(c.getCustomerId(), c.getCustomerName(), c.getTelephone(),
                c.getEmail(), c.getCustomerBirthday(), c.getCustomerStatus());
    }
}
