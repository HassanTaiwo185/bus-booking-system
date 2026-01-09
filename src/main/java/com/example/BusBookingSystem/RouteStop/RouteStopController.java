package com.example.BusBookingSystem.RouteStop;

import com.example.BusBookingSystem.Routes.Routes;
import com.example.BusBookingSystem.Routes.RoutesRepository;
import com.example.BusBookingSystem.Stops.StopsRepository;
import com.example.BusBookingSystem.User.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RouteStopController {

    private final RouteStopService routeStopService;
    private final RoutesRepository routesRepository;
    private final StopsRepository stopsRepository;
    private final RouteStopRepository routeStopRepository;


    @GetMapping("/route-stops/create")
    public String createRouteStops(
            @RequestParam(required = false) Long routeId,
            Model model
    ) {
        model.addAttribute("routes", routesRepository.findAll());


        if (!model.containsAttribute("routeStop")) {
            RouteStopDto dto = new RouteStopDto();
            dto.setRouteId(routeId);
            model.addAttribute("routeStop", dto);
        }

        if (routeId != null) {
            model.addAttribute("stops", stopsRepository.findAll());
        }

        return "route-stops-create";
    }



    @PostMapping("/route-stops")
    public String saveRouteStops(
            @Valid @ModelAttribute("routeStop") RouteStopDto routeStopDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.routeStop", bindingResult);
            redirectAttributes.addFlashAttribute("routeStop", routeStopDto);
            return "redirect:/route-stops/create?routeId=" + routeStopDto.getRouteId();
        }

        try {
            routeStopService.saveRouteStops(routeStopDto);
            redirectAttributes.addFlashAttribute("success", "Route sequence updated successfully!");
            return "redirect:/route-stops/manage";

        } catch (RuntimeException ex) {

            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/route-stops/create?routeId=" + routeStopDto.getRouteId();

        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "An unexpected system error occurred. Please try again.");
            return "redirect:/admin-dashboard";
        }


    }


    @GetMapping("/route-stops/manage")
    public String manageRouteStops(Model model) {

        List<RouteStop> allActive = routeStopRepository.findAllActive();

        Map<Routes, List<RouteStop>> groupedStops = allActive.stream()
                .collect(Collectors.groupingBy(
                        RouteStop::getRoute,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        model.addAttribute("groupedStops", groupedStops);
        return "route-stops-manage";
    }


    @InitBinder
    public void initBinder(WebDataBinder binder) {

        // Allows Spring to grow the List if the index from the HTML is higher than the current size
        binder.setAutoGrowNestedPaths(true);
        binder.setAutoGrowCollectionLimit(1000);
    }
}
