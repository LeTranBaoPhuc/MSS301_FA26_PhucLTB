package com.mss301.inventory_service.controller;

import com.mss301.inventory_service.dto.ReserveRequest;
import com.mss301.inventory_service.entity.*;
import com.mss301.inventory_service.repository.*;
import com.mss301.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final ProductRepository productRepository;
    private final ReservationRepository reservationRepository;

    @PostMapping("/reserve")
    public Reservation reserve(@RequestBody ReserveRequest req) {
        return inventoryService.reserve(req);
    }

    @PostMapping("/release/{orderId}")
    public String release(@PathVariable Long orderId) {
        inventoryService.release(orderId);
        return "RELEASED";
    }

    @GetMapping("/products")
    public List<Product> products() {
        return productRepository.findAll();
    }

    @GetMapping("/reservations")
    public List<Reservation> reservations() {
        return reservationRepository.findAll();
    }
}
