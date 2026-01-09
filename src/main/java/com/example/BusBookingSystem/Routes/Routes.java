package com.example.BusBookingSystem.Routes;


import com.example.BusBookingSystem.RouteStop.RouteStop;
import com.example.BusBookingSystem.User.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Table(name = "routes")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Routes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 100)
    private String routeName;


    @Column(nullable = false)
    private BigDecimal distance;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 50)
    private Status status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User createdBy;


    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    @OneToMany(
            mappedBy = "route",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("stopOrder ASC")
    private List<RouteStop> routeStops;




}
