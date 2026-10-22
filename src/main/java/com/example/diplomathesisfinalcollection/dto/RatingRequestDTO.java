package com.example.diplomathesisfinalcollection.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RatingRequestDTO {

    @NotBlank(message = "Appointment ID is required.")
    private String appointmentId;

    @NotBlank(message = "Doctor ID is required.")
    private String doctorId;

    @NotNull(message = "Score is required.")
    @Min(value = 1, message = "Score must be at least 1.")
    @Max(value = 5, message = "Score must be at most 5.")
    private Integer score;

    private String comment; // Optional

    @NotNull
    private Boolean isAnonymous;
}
