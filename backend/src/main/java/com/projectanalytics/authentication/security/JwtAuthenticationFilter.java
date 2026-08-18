package com.projectanalytics.authentication.security;

import com.projectanalytics.authentication.persistence.UserEntity;
import com.projectanalytics.authentication.persistence.UserRepository;
import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Extracts and validates Bearer JWT tokens on each request.
 * Re-checks enabled + credentialsVersion against the database (password reset invalidation).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final SecurityErrorWriter securityErrorWriter;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository,
            SecurityErrorWriter securityErrorWriter
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.securityErrorWriter = securityErrorWriter;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                AuthenticatedUser fromJwt = jwtService.parseAuthenticatedUser(token);
                UserEntity user = userRepository.findById(fromJwt.getId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_003));
                if (!user.isEnabled()) {
                    throw new BusinessException(ErrorCode.AUTH_005);
                }
                if (user.getCredentialsVersion() != fromJwt.getCredentialsVersion()) {
                    throw new BusinessException(ErrorCode.AUTH_003, "Session expired after password change.");
                }
                AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                        user.getId(),
                        user.getUsername(),
                        user.getPasswordHash(),
                        user.getRole(),
                        user.isEnabled(),
                        user.getCredentialsVersion()
                );
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                authenticatedUser,
                                null,
                                authenticatedUser.getAuthorities()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        } catch (BusinessException exception) {
            SecurityContextHolder.clearContext();
            securityErrorWriter.write(response, request.getRequestURI(), exception.getErrorCode());
        } catch (Exception exception) {
            SecurityContextHolder.clearContext();
            securityErrorWriter.write(response, request.getRequestURI(), ErrorCode.AUTH_003);
        }
    }
}
