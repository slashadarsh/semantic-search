package com.springai.semanticsearch.dto;

import jakarta.persistence.Enumerated;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProfileCreateRequest {
    private String name;

    private String description;

    private String expression;

    @Enumerated
    private SetTopBoxType boxType;
}
