package com.backend.gapfinder.dto.request;

import com.backend.gapfinder.enums.ActivityEffortEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

    @NotBlank(message = "El nombre es obligatorio")
    String name,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    String password,

    @NotBlank(message = "El programa es obligatorio")
    String program,

    @NotBlank(message = "El semestre es obligatorio")
    String semester,

    @NotNull(message = "La preferencia de esfuerzo es obligatoria")
    ActivityEffortEnum activityEffortPreference
) {} 