package edu.cit.chan.unilost.features.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private String id;
    private String type;
    private String title;
    private String message;
    private String linkId;
    private boolean isRead;
    private LocalDateTime createdAt;
}
