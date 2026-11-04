package com.mss301.brand.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandWatchesResponse {

    private Integer brandId;
    private String brandName;
    private Integer watchCount;
    private List<Integer> watchIds;
}
