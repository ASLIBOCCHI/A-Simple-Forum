package com.example.model;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class Reply {
    private String replyId = UUID.randomUUID().toString();
    private String authorId;
    private String authorUsername;
    private String text;
    private Instant repliedAt = Instant.now();
}
