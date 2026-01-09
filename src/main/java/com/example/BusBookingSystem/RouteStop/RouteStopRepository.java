package com.example.BusBookingSystem.RouteStop;

import com.example.BusBookingSystem.Routes.Routes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {

    List<RouteStop> findByRouteAndActiveTrue(Routes route);

    List<RouteStop> findByRouteAndActiveTrueOrderByStopOrderAsc(Routes route);

    @Query("""
select rs
from RouteStop rs
where rs.active = true
order by rs.route.id, rs.stopOrder
""")
    List<RouteStop> findAllActive();


    List<RouteStop> findByActiveTrueOrderByRouteIdAscStopOrderAsc();




}
