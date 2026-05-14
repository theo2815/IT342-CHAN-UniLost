package edu.cit.chan.unilost.features.chat;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class MessageDTO {
    private String id;
    private String chatId;
    private String senderId;
    private String senderName;
    private String content;
    private String type;
    private Map<String, Object> metadata;
    private boolean read;
    private LocalDateTime createdAt;
}
