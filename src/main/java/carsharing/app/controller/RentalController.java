package carsharing.app.controller;

import carsharing.app.dto.rental.RentalDto;
import carsharing.app.dto.rental.RentalRequestDto;
import carsharing.app.service.interfaces.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Rental management", description = "Endpoints for rental management")
@RequiredArgsConstructor
@RequestMapping("/rentals")
public class RentalController {
    private final RentalService rentalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add new rental", description = "Add new rental")
    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    public RentalDto addNewRental(@AuthenticationPrincipal UserDetails userDetails,
                                  @RequestBody @Valid RentalRequestDto requestDto) {
        return rentalService.addNewRental(userDetails.getUsername(), requestDto);
    }

    @GetMapping("/{userId}/all")
    @Operation(summary = "Get rentals", description = "Get rentals by user id")
    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    public Page<RentalDto> getAllActualRentalsByUserId(@PathVariable Long userId,
                                                 @AuthenticationPrincipal UserDetails userDetails,
                                                 @RequestParam boolean isActive,
                                                 @PageableDefault(size = 10, page = 0)
                                                 Pageable pageable) {
        return rentalService.getAllActualRentalsByUserId(userId, userDetails.getUsername(),
                isActive, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get rental", description = "Get rental by id")
    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    public RentalDto getRentalById(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        return rentalService.getSpecificRentalById(id, userDetails.getUsername());
    }

    @PutMapping("/{id}/return")
    @Operation(summary = "Set return date", description = "Set actual return date")
    @PreAuthorize("hasRole('MANAGER')")
    public RentalDto setActualReturnDate(@PathVariable Long id) {
        return rentalService.setActualReturnDate(id);
    }
}
