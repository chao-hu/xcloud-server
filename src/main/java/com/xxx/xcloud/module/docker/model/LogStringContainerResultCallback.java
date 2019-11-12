///**
// *
// */
//package com.xxx.xcloud.module.docker.model;
//
//import com.bonc.bdos.service.application.service.impl.ApplicationImpl;
//import com.github.dockerjava.api.model.Frame;
//import com.github.dockerjava.core.command.LogContainerResultCallback;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.UnsupportedEncodingException;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author lkx
// *
// */
//public class LogStringContainerResultCallback extends LogContainerResultCallback {
//    private static final Logger LOG = LoggerFactory.getLogger(ApplicationImpl.class);
//
//    protected final StringBuffer log = new StringBuffer();
//
//    List<Frame> collectedFrames = new ArrayList<Frame>();
//
//    boolean collectFrames = false;
//
//    public LogStringContainerResultCallback() {
//        this(false);
//    }
//
//    public LogStringContainerResultCallback(boolean collectFrames) {
//        this.collectFrames = collectFrames;
//    }
//
//    @Override
//    public void onNext(Frame frame) {
//        if (collectFrames) {
//            collectedFrames.add(frame);
//        }
//        try {
//            log.append(new String(frame.getPayload(), "UTF8"));
//        } catch (UnsupportedEncodingException e) {
//            LOG.error("error:", e);
//        }
//    }
//
//    @Override
//    public String toString() {
//        return log.toString();
//    }
//
//    public List<Frame> getCollectedFrames() {
//        return collectedFrames;
//    }
//}
