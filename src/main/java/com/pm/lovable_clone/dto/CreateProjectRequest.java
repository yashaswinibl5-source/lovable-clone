package com.pm.lovable_clone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data  //lombok generates getters, setters, toString(), equals(), hashcoade()
public class CreateProjectRequest {
  //validation
    @NotBlank(message = "Project title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private String template = "REACT_VITE_TAILWIND";
}
