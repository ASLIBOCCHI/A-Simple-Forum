package com.example.dto;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
@Data public class PostRequest { @NotBlank private String content; }
