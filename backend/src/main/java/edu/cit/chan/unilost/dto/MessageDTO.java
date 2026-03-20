package edu.cit.chan.unilost.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private String id;
    private String chatId;
    private String senderId;
    private String senderName;
    private String content;
    private boolean read;
    private LocalDateTime createdAt;
}
