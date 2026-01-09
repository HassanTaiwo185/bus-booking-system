package com.example.BusBookingSystem.Bus;

import com.example.BusBookingSystem.User.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "buses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bus {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bus_number", unique = true, nullable = false)
    private String busNumber;

    @Column(nullable = false)
    @Positive
    private Long capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @Column(nullable = false)
    @Positive(message = "Price must be greater than zero")
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

}
