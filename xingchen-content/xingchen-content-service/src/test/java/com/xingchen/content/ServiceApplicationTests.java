package com.xingchen.content;


import com.xingchen.content.mapper.CourseCategoryMapper;
import com.xingchen.content.model.dto.CourseCategoryTreeDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import javax.annotation.Resource;
import java.util.List;
@SpringBootTest(classes = ServiceApplicationTests.class)
class ServiceApplicationTests {

    @Test
    void contextLoads() {
    }
    @Resource
    CourseCategoryMapper courseCategoryMapper;


    @Test
    void testCourseCategoryMapper() {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes("1");
        System.out.println(courseCategoryTreeDtos);
    }

}
