package com.xingchen.content.api;

import com.xingchen.content.model.dto.SaveTeachplanDto;
import com.xingchen.content.model.dto.TeachplanDto;
import com.xingchen.content.service.TeachplanService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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

     @Autowired
     TeachplanService teachplanService;

  @GetMapping("/teachplan/{courseId}/tree-nodes")
  public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId){
    return teachplanService.findTeachplayTree(courseId);
  }

  @PostMapping("/teachplan")
  public void saveTeachplan(@RequestBody SaveTeachplanDto dto){
      teachplanService.saveTeachplan(dto);
  }
}
