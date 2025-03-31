package com.springai.semanticsearch.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BroadcasterCreateRequest {
    private String displayName;
    private String broadcasterCode;
    private String invidiId;
}
