package com.xingchen.model.dto;



import com.xingchen.model.po.CourseCategory;
import lombok.Data;

import java.util.List;

/**
 * @author mrt
 * @version 1.0
 * @description TODO
 * @date 2022/12/10 10:08
 */
@Data
public class CourseCategoryTreeDto extends CourseCategory {
 List childrenTreeNodes;
}
