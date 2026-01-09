package com.example.BusBookingSystem.Routes;

import com.example.BusBookingSystem.User.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoutesController {

    private final RoutesService routesService;


    @GetMapping("/routes")
    public String listRoutes(
            @RequestParam(defaultValue = "routes") String mode,
            Model model
    ) {
        model.addAttribute("routes", routesService.getAllRoutes());
        model.addAttribute("mode", mode);
        return "routes-list";
    }


    @GetMapping("/routes/new")
    public String createRoutePage(Model model) {

        if (!model.containsAttribute("routes")) {
            model.addAttribute("routes", new RoutesRequestDto());
        }

        return "routes-create";
    }


    @PostMapping("/routes")
    public String createRoute(
            @Valid @ModelAttribute("routes") RoutesRequestDto request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.routes",
                    bindingResult
            );
            redirectAttributes.addFlashAttribute("routes", request);
            return "redirect:/routes/new";
        }

        User currentUser = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        routesService.addRoute(request, currentUser);

        redirectAttributes.addFlashAttribute("success", "Route created successfully");
        return "redirect:/routes";
    }


    @GetMapping("/routes/{id}/edit")
    public String editRoutePage(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("routes")) {

            model.addAttribute("routes", routesService.getRouteForEdit(id));
        }
        return "routes-edit";
    }



    @PostMapping("/routes/{id}")
    public String updateRoute(
            @PathVariable Long id,
            @Valid @ModelAttribute("routes") RoutesRequestDto request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.routes",
                    bindingResult
            );
            redirectAttributes.addFlashAttribute("routes", request);
            return "redirect:/routes/" + id + "/edit";
        }

        User currentUser = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        routesService.updateRoute(id, request, currentUser);

        redirectAttributes.addFlashAttribute("success", "Route updated successfully");
        return "redirect:/routes";
    }
}
