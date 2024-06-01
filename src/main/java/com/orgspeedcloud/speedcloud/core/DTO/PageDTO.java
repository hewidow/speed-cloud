package com.orgspeedcloud.speedcloud.core.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页对象
 * @author Chen
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageDTO {
    private Integer pageSize;
    private Integer totalRecordNumber;
    private Integer pageNumber;
    private Integer totalPageNumber;
    private List<?> data;
}
