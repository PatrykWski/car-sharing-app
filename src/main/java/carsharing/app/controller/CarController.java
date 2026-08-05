package carsharing.app.controller;

import carsharing.app.dto.car.CarDto;
import carsharing.app.dto.car.CarRequest;
import carsharing.app.dto.car.UpdateCarRequest;
import carsharing.app.service.interfaces.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
@Tag(name = "Car management", description = "Endpoints for car management")
public class CarController {
    private final CarService carService;

    @PostMapping
    @Operation(summary = "Add a car", description = "Add a car to the database")
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    public CarDto addNewCar(@RequestBody @Valid CarRequest carRequest) {
        return carService.addNewCar(carRequest);
    }

    @GetMapping
    @Operation(summary = "Get all cars", description = "Get all cars sorted by brand")
    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    public Page<CarDto> getPageOfCars(
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "brand")
            Pageable pageable) {
        return carService.getPageOfCars(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a car", description = "Get a car by his ID")
    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    public CarDto getCarById(@PathVariable Long id) {
        return carService.getCarById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a car", description = "Update car's inventory or daily's fee")
    @PreAuthorize("hasRole('MANAGER')")
    public CarDto updateCarById(@PathVariable Long id,
                                @RequestBody @Valid UpdateCarRequest carRequest) {
        return carService.updateCarById(id, carRequest);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a car", description = "Soft delete a car")
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCar(@PathVariable Long id) {
        carService.deleteCarById(id);
    }
}
