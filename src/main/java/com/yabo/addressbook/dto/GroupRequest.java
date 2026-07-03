package com.yabo.addressbook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "分组创建/更新请求体")
public class GroupRequest {

    @NotBlank(message = "分组名称不能为空")
    @Size(max = 50, message = "分组名称长度不能超过50个字符")
    @Schema(description = "分组名称", example = "同事")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
