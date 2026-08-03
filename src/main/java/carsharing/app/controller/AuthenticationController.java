package carsharing.app.controller;

import carsharing.app.dto.auth.LoginDto;
import carsharing.app.dto.auth.LoginRequestDto;
import carsharing.app.dto.register.UserRequestDto;
import carsharing.app.dto.register.UserResponseDto;
import carsharing.app.security.AuthenticationService;
import carsharing.app.service.interfaces.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication management", description = "Endpoints for authentication management")
public class AuthenticationController {
    private final RegistrationService registrationService;
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register new user", description = "Register new user")
    public UserResponseDto register(@Valid @RequestBody UserRequestDto userRequestDto) {
        return registrationService.register(userRequestDto);
    }

    @PostMapping("/login")
    @Operation(summary = "Log in a user", description = "Login a user and return a token")
    public LoginDto login(@Valid @RequestBody LoginRequestDto requestDto) {
        return authenticationService.authenticate(requestDto);
    }
}
