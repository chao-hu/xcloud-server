package com.xxx.xcloud.module.ci.strategy.jenkins.strategy.compile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.module.devops.model.BuildModel;
import com.xxx.xcloud.module.devops.model.GradleModel;
import com.xxx.xcloud.module.devops.model.MvnModel;
import com.xxx.xcloud.module.ci.model.CiInvokeJava;
import com.xxx.xcloud.module.ci.model.CiInvokeJavaGradle;
import com.xxx.xcloud.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.util.Objects;

/**
 * java编译工具枚举策略
 * 
 * @author mengaijun
 * @date: 2019年8月6日 下午3:18:34
 */
public enum JavaCompileStrategyEnum {
    /**
     * maven编译
     */
    MAVEN {

        @Override
        public BuildModel generateBuildModel(String compileJsonData) {

            BuildModel buildModel = new BuildModel();
            CiInvokeJava ciInvokeJava = JSONArray.parseObject(compileJsonData, CiInvokeJava.class);
            MvnModel mvnModel = new MvnModel();
            buildModel.setMvnModel(mvnModel);

            mvnModel.setBuildcmd(ciInvokeJava.getBuildcmd());
            mvnModel.setMvnVersion(ciInvokeJava.getMvnVersion());

            return buildModel;
        }

        @Override
        public ApiResult checkCompile(String compileJsonData) {
            CiInvokeJava ciInvokeJava = transferJsonToObj(compileJsonData, CiInvokeJava.class);
            if (ciInvokeJava == null || StringUtils.isEmpty(ciInvokeJava.getBuildcmd())
                    || StringUtils.isEmpty(ciInvokeJava.getLangVersion())
                    || StringUtils.isEmpty(ciInvokeJava.getMvnVersion())) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                        "编译信息需要为json格式, 且包含buildcmd, langVersion, mvnVersion信息!");
            }
            return null;
        }

    },

    /**
     * gradle 编译
     */
    GRADLE {

        @Override
        public BuildModel generateBuildModel(String compileJsonData) {

            BuildModel buildModel = new BuildModel();
            CiInvokeJavaGradle ciInvokeJavaGradle = JSONArray.parseObject(compileJsonData, CiInvokeJavaGradle.class);
            GradleModel gradleModel = new GradleModel();
            BeanUtils.copyProperties(ciInvokeJavaGradle, gradleModel);
            buildModel.setGradleModel(gradleModel);

            return buildModel;
        }

        @Override
        public ApiResult checkCompile(String compileJsonData) {

            CiInvokeJavaGradle ciInvokeJavaGradle = transferJsonToObj(compileJsonData, CiInvokeJavaGradle.class);
            if (ciInvokeJavaGradle == null) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "编译信息不符合json格式！");
            }
            if (StringUtils.isEmpty(ciInvokeJavaGradle.getTasks())) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "编译命令不能为空！");
            }
            // 使用项目自带的gradle工具
            if (Objects.equals(ciInvokeJavaGradle.getUseWrapper(), Boolean.TRUE)) {
                if (StringUtils.isEmpty(ciInvokeJavaGradle.getWrapperLocation())) {
                    return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "使用项目自带gradle，请填写gradle路径！");
                }
            } else { // 使用jenkins配置的gradle
                if (StringUtils.isEmpty(ciInvokeJavaGradle.getGradleName())) {
                    return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "gradle版本信息不能为空！");
                }
            }

            return null;
        }

    };

    /**
     * 根据编译信息获取build对象
     * 
     * @param compileJsonData
     * @return BuildModel
     * @date: 2019年8月6日 下午3:57:35
     */
    public abstract BuildModel generateBuildModel(String compileJsonData);

    /**
     * 检测编译信息是否符合条件
     * 
     * @param compileJsonData
     * @return ApiResult
     * @date: 2019年8月12日 下午4:42:52
     */
    public abstract ApiResult checkCompile(String compileJsonData);

    /**
     * json字符串转为指定格式
     * 
     * @param compile
     * @param clazz
     * @return T
     * @date: 2019年8月13日 上午10:32:47
     */
    private static <T> T transferJsonToObj(String compile, Class<T> clazz) {
        try {
            return JSONObject.parseObject(compile, clazz);
        } catch (Exception e) {
            LOG.error("传入字符串不符合json格式!", e);
        }
        return null;
    }

    private static final Logger LOG = LoggerFactory.getLogger(JavaCompileStrategyEnum.class);

}
