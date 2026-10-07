package com.fudn.booking_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    private Long customerId;
    
    // String id do MongoDB tao ra ben movie-service
    private String showtimeId;
    
    private LocalDateTime bookingTime;
    private BigDecimal totalPrice;

    // TODO 7.3: Quan he 1-N mappedBy "booking" trong class Ticket, xoa booking se xoa cac ticket
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets = new ArrayList<>();
    
    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
        ticket.setBooking(this);
    }
}
