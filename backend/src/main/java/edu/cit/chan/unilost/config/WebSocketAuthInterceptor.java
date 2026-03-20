package edu.cit.chan.unilost.config;

import edu.cit.chan.unilost.entity.AccountStatus;
import edu.cit.chan.unilost.entity.UserEntity;
import edu.cit.chan.unilost.repository.UserRepository;
import edu.cit.chan.unilost.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Authenticates STOMP CONNECT frames using the JWT token
 * passed in the "Authorization" header.
 */
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtils.validateToken(token)) {
                    String email = jwtUtils.getEmailFromToken(token);
                    UserEntity user = userRepository.findByEmail(email).orElse(null);
                    if (user != null && user.getAccountStatus() == AccountStatus.ACTIVE) {
                        String role = user.getRole().name();
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        email, null,
                                        List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                        accessor.setUser(auth);
                    }
                }
            }
            // If no valid auth, user remains null — subscriptions will proceed anonymously
            // We block unauthorized subscriptions in the SUBSCRIBE handler below
        }

        // Block SUBSCRIBE if user is not authenticated
        if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            if (accessor.getUser() == null) {
                throw new org.springframework.messaging.MessageDeliveryException("Not authenticated");
            }
        }

        // Block SEND if user is not authenticated
        if (accessor != null && StompCommand.SEND.equals(accessor.getCommand())) {
            if (accessor.getUser() == null) {
                throw new org.springframework.messaging.MessageDeliveryException("Not authenticated");
            }
        }

        return message;
    }
}
