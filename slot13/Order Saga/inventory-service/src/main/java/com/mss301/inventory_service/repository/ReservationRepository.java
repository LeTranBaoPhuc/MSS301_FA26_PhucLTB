package com.mss301.inventory_service.repository;

import com.mss301.inventory_service.entity.Reservation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReservationRepository extends MongoRepository<Reservation, Long> {
}
