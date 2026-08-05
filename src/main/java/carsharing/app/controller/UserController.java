package carsharing.app.controller;

import carsharing.app.dto.user.UserDto;
import carsharing.app.dto.user.UserUpdateDto;
import carsharing.app.model.RoleName;
import carsharing.app.service.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Managing authentication and user registration",
        description = "Endpoints for user management")
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PutMapping("/{id}/role")
    @Operation(summary = "Update user role", description = "Update user role by user id")
    @PreAuthorize("hasRole('MANAGER')")
    public UserDto updateUserRole(@PathVariable Long id, @RequestParam RoleName roleName) {
        return userService.updateUserRole(id, roleName);
    }

    @GetMapping("/me")
    @Operation(summary = "See my profile", description = "See my profile")
    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    public UserDto getUser(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.getUser(userDetails.getUsername());
    }

    @PutMapping("/me")
    @Operation(summary = "Update my profile", description = "Update my profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public UserDto updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestBody UserUpdateDto userUpdateDto) {
        return userService.updateProfile(userDetails.getUsername(), userUpdateDto);
    }
}
