package com.mss301.brand.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentResponse {

    private Integer watchId;
    private Integer brandId;
    private Integer previousBrandId;
    private String status;
}
