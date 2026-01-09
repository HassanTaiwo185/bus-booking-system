package com.example.BusBookingSystem.Bus;

import com.example.BusBookingSystem.User.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequiredArgsConstructor
@Controller
@PreAuthorize("hasRole('ADMIN')")
public class BusController {

    private final BusService busService;

    @GetMapping("/bus/list")
    public String listBuses(Model model) {
        model.addAttribute("buses", busService.getAllBuses());
        return "buses-list";
    }

    @GetMapping("bus/new")
    public String createBusPage(Model model) {
        if (!model.containsAttribute("bus")) {
            model.addAttribute("bus", new BusRequestDto());
        }
        model.addAttribute("types", BusType.values());
        model.addAttribute("statuses", BusStatus.values());
        return "buses-create";
    }

    @PostMapping("bus/create")
    public String createBus(@Valid @ModelAttribute("bus") BusRequestDto dto,
                            BindingResult result,
                            @AuthenticationPrincipal User user,
                            RedirectAttributes ra) {

        if (result.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.bus", result);
            ra.addFlashAttribute("bus", dto);
            return "redirect:/bus/new";
        }

        try {

            busService.addBus(dto, user);
            ra.addFlashAttribute("success", "Bus added successfully!");
            return "redirect:/bus/list";

        } catch (RuntimeException e) {

            ra.addFlashAttribute("error", e.getMessage());
            ra.addFlashAttribute("bus", dto);
            return "redirect:/bus/new";
        }
    }
    @GetMapping("/bus/{id}/edit")
    public String editBusPage(@PathVariable Long id, Model model) {
        model.addAttribute("bus", busService.getBusForEdit(id));

        model.addAttribute("types", BusType.values());
        model.addAttribute("statuses", BusStatus.values());
        return "buses-edit";
    }
    @PostMapping("/bus/{id}")
    public String updateBus(@PathVariable Long id,
                            @Valid @ModelAttribute("bus") BusRequestDto dto,
                            BindingResult result,
                            RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.bus", result);
            ra.addFlashAttribute("bus", dto);
            return "redirect:/bus/" + id + "/edit";
        }
        busService.updateBus(id, dto);
        ra.addFlashAttribute("success", "Bus updated successfully!");
        return "redirect:/bus/list";
    }
}
