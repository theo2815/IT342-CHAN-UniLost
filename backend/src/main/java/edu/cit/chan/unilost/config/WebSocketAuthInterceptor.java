package edu.cit.chan.unilost.config;

import edu.cit.chan.unilost.entity.ChatEntity;
import edu.cit.chan.unilost.features.user.AccountStatus;
import edu.cit.chan.unilost.features.user.UserEntity;
import edu.cit.chan.unilost.features.user.UserRepository;
import edu.cit.chan.unilost.repository.ChatRepository;
import edu.cit.chan.unilost.features.auth.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Authenticates STOMP CONNECT frames using the JWT token
 * passed in the "Authorization" header.
 */
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Pattern CHAT_TOPIC_PATTERN = Pattern.compile("^/topic/chat/([a-zA-Z0-9]+)$");

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;

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
            // Reject unauthenticated CONNECT frames to prevent resource consumption
            if (accessor.getUser() == null) {
                throw new MessageDeliveryException("Authentication required for WebSocket connection");
            }
        }

        // Block SUBSCRIBE if user is not authenticated, and verify participant authorization for chat topics
        if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            if (accessor.getUser() == null) {
                throw new MessageDeliveryException("Not authenticated");
            }

            String destination = accessor.getDestination();
            if (destination != null) {
                Matcher matcher = CHAT_TOPIC_PATTERN.matcher(destination);
                if (matcher.matches()) {
                    String chatId = matcher.group(1);
                    String email = accessor.getUser().getName();
                    UserEntity user = userRepository.findByEmail(email).orElse(null);
                    if (user == null) {
                        throw new MessageDeliveryException("User not found");
                    }
                    ChatEntity chat = chatRepository.findById(chatId).orElse(null);
                    if (chat == null) {
                        throw new MessageDeliveryException("Chat not found");
                    }
                    if (!chat.getFinderId().equals(user.getId()) && !chat.getOwnerId().equals(user.getId())) {
                        throw new MessageDeliveryException("Not a participant of this chat");
                    }
                }
            }
        }

        // Block SEND if user is not authenticated
        if (accessor != null && StompCommand.SEND.equals(accessor.getCommand())) {
            if (accessor.getUser() == null) {
                throw new MessageDeliveryException("Not authenticated");
            }
        }

        return message;
    }
}
