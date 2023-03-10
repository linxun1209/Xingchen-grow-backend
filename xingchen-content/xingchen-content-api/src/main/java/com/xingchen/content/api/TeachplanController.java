package com.xingchen.content.api;

import com.xingchen.content.model.dto.BindTeachplanMediaDto;
import com.xingchen.content.model.dto.SaveTeachplanDto;
import com.xingchen.content.model.dto.TeachplanDto;
import com.xingchen.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description TODO
 * @author Mr.M
 * @date 2022/10/10 11:55
 * @version 1.0
 */
@Api(value = "课程计划接口",tags = "课程计划接口")
@RestController
public class TeachplanController {

    @Resource
    TeachplanService teachplanService;

    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId) {
        return teachplanService.findTeachplanTree(courseId);
    }

    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachplanDto dto) {
        teachplanService.saveTeachplan(dto);
    }

    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto) {
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }
}
