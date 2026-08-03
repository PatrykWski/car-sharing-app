package carsharing.app.security;

import carsharing.app.dto.auth.LoginDto;
import carsharing.app.dto.auth.LoginRequestDto;
import carsharing.app.exception.LoginException;
import carsharing.app.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public LoginDto authenticate(LoginRequestDto loginRequestDto) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getEmail(), loginRequestDto.getPassword()));
            User user = (User) authentication.getPrincipal();
            String jwtToken = jwtUtil.createToken(user);
            return new LoginDto(jwtToken);
        } catch (AuthenticationException ex) {
            throw new LoginException("Invalid login or password");
        }
    }
}
