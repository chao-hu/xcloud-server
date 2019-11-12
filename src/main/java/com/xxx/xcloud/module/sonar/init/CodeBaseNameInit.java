//package com.xxx.xcloud.module.sonar.init;
//
//import com.xxx.xcloud.module.ci.entity.Ci;
//import com.xxx.xcloud.module.ci.entity.CiCodeCredentials;
//import com.xxx.xcloud.module.ci.entity.CodeInfo;
//import com.xxx.xcloud.module.ci.service.CiCodeCredentialsService;
//import com.xxx.xcloud.module.ci.service.ICiService;
//import com.xxx.xcloud.module.image.consts.ImageConstant;
//import com.xxx.xcloud.module.image.entity.Image;
//import com.xxx.xcloud.module.image.entity.ImageVersion;
//import com.xxx.xcloud.module.image.repository.ImageVersionRepository;
//import com.xxx.xcloud.module.image.service.ImageService;
//import com.xxx.xcloud.module.sonar.entity.CodeCheckResult;
//import com.xxx.xcloud.module.sonar.entity.CodeCheckTask;
//import com.xxx.xcloud.module.sonar.repository.CodeCheckResultRepository;
//import com.xxx.xcloud.module.sonar.repository.CodeCheckTaskRepository;
//import com.xxx.xcloud.module.sonar.service.SonarService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
///**
// * 镜像版本表和代码检查结果表， codeBaseName字段 初始化
// *
// * @author mengaijun
// * @Description: TODO
// * @date: 2019年8月21日 下午2:56:23
// */
//@Component
//@Order(value = Integer.MAX_VALUE)
//public class CodeBaseNameInit implements CommandLineRunner {
//    private static Logger logger = LoggerFactory.getLogger(CodeBaseNameInit.class);
//    @Autowired
//    ImageVersionRepository imageVersionRepository;
//
//    @Autowired
//    ImageService imageService;
//
//    @Autowired
//    ICiService ciService;
//
//    @Autowired
//    CiCodeCredentialsService ciCodeCredentialsService;
//
//    @Autowired
//    CodeCheckResultRepository codeCheckResultRepository;
//
//    @Autowired
//    SonarService sonarService;
//
//    @Autowired
//    CodeCheckTaskRepository codeCheckTaskRepository;
//
//    @Override
//    public void run(String... args) throws Exception {
//        // 设置镜像版本表中，由代码构建而来的镜像，的代码信息
//        try {
//            List<ImageVersion> imageVersions = imageVersionRepository
//                    .findByCiTypeAndCodeBaseNameIsNull(ImageConstant.IMAGE_CI_TYPE_CODECI);
//            for (ImageVersion imageVersion : imageVersions) {
//                CiThreadPool.getExecotur().execute(() -> {
//                    updateImageVersonCodeBaseNameInfo(imageVersion);
//                });
//            }
//        } catch (Exception e) {
//            logger.error("查询代码构建生成，代码信息为空的镜像错误！", e);
//        }
//
//        /**********************
//         * 因为只取每天最新的一条数据，不需要设置之前的数据了
//         *************************/
//        // 设置代码检查结果表中，代码信息为空的记录的代码信息
//        // key：sonar任务ID value：sonar任务ID对应所有的检查结果
//        // Map<String, List<CodeCheckResult>> map = new HashMap<>();
//        // try {
//        // List<CodeCheckResult> codeCheckResults =
//        // codeCheckResultRepository.findByCodeBaseNameIsNull();
//        // for (CodeCheckResult codeCheckResult : codeCheckResults) {
//        // if (map.get(codeCheckResult.getSonarTaskId()) == null) {
//        // List<CodeCheckResult> list = new ArrayList<CodeCheckResult>();
//        // list.add(codeCheckResult);
//        // list.add(codeCheckResult);
//        // map.put(codeCheckResult.getSonarTaskId(), list);
//        // } else {
//        // map.get(codeCheckResult.getSonarTaskId()).add(codeCheckResult);
//        // }
//        // }
//        // Integer count = 0;
//        // map.forEach((id, codeCheckResultList) -> {
//        // CiThreadPool.getExecotur().execute(() -> {
//        // updateCodeCheckResultCodeBaseNameInfo(id, codeCheckResultList);
//        // });
//        // try {
//        // Thread.sleep(Integer.MAX_VALUE);
//        // } catch (InterruptedException e) {
//        // e.printStackTrace();
//        // }
//        // });
//        // } catch (Exception e) {
//        // logger.error("查询代码构建生成，代码信息为空的镜像错误！", e);
//        // }
//
//    }
//
//    /**
//     * 更新镜像版本的代码信息
//     *
//     * @param imageVersion
//     *            void
//     * @date: 2019年8月21日 下午3:47:16
//     */
//    private void updateImageVersonCodeBaseNameInfo(ImageVersion imageVersion) {
//        try {
//            Image image = imageService.getImageById(imageVersion.getImageId());
//            Ci ci = ciService.getCiByImageNameAndVersion(image.getTenantName(), image.getImageName(),
//                    imageVersion.getImageVersion());
//            if (ci == null || ci.getCodeInfoId() == null) {
//                return;
//            }
//
//            CodeInfo codeInfo = ciCodeCredentialsService.getCodeInfoById(ci.getCodeInfoId());
//            if (codeInfo == null) {
//                return;
//            }
//
//            CiCodeCredentials ciCodeCredentials = ciCodeCredentialsService.getById(codeInfo.getCiCodeCredentialsId());
//            if (ciCodeCredentials == null) {
//                return;
//            }
//
//            imageVersion.setCodeBaseName(
//                    CiServiceImpl.generateCodeBaseName(ciCodeCredentials, codeInfo, ci.getTenantName()));
//
//            imageVersionRepository.save(imageVersion);
//        } catch (Exception e) {
//            logger.error("更新镜像版本的代码库信息错误", e);
//        }
//
//    }
//
//    /**
//     * 更新代码检查结果的检查信息
//     *
//     * @param
//     * @param
//     *
//     * @date: 2019年8月21日 下午4:59:42
//     */
//    private void updateCodeCheckResultCodeBaseNameInfo(String taskId, List<CodeCheckResult> codeCheckResults) {
//        try {
//
//            CodeCheckTask codeCheckTask = codeCheckTaskRepository.getById(taskId);
//            if (codeCheckTask == null) {
//                return;
//            }
//            CodeInfo codeInfo = ciCodeCredentialsService.getCodeInfoById(codeCheckTask.getCodeInfoId());
//            if (codeInfo == null) {
//                return;
//            }
//            CiCodeCredentials ciCodeCredentials = ciCodeCredentialsService.getById(codeInfo.getCiCodeCredentialsId());
//            codeCheckResults.forEach((codeCheckResult) -> {
//                codeCheckResult.setCodeBaseName(
//                        CiServiceImpl.generateCodeBaseName(ciCodeCredentials, codeInfo, codeCheckTask.getTenantName()));
//            });
//
//            codeCheckResultRepository.saveAll(codeCheckResults);
//        } catch (Exception e) {
//            logger.error("更新代码检查结果的代码库信息错误", e);
//        }
//    }
//
//}
