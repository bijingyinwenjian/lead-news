package com.heima.model.wemedia.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class WmNewsDownOrUpDto implements Serializable {
    private Integer id;
    private Short enable;
}
