package com.xxx.xcloud.rest.v1;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.ReturnCode;

import io.swagger.annotations.ApiOperation;
import lombok.Data;

/**
 * @ClassName: TestController
 * @Description: TestController
 * @author huchao
 * @date 2019年10月24日
 *
 */
@Controller
@RequestMapping("/v1/")
public class TestController {

    @ResponseBody
    @RequestMapping(value = { "" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取租户", notes = "")
    public ApiResult get() {

        User user = new User();
        user.setAge(null);
        user.setName(null);
        user.setSex("女1");
        user.setXxx(new ArrayList<>());

        return new ApiResult(ReturnCode.CODE_SUCCESS, user, "获取租户成功");
    }

    @Data
    class User {

        private String name;
        private Integer age;
        private String sex;
        private List<String> xxx;

    }
}
