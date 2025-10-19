package com.thirikkale.scheduledrideservice.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErrorResponseDto {
    private String error;
    private String message;
}
