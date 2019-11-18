package com.xxx.xcloud.rest.v1.service.model;

import java.io.Closeable;
import java.io.OutputStreamWriter;

import javax.servlet.ServletOutputStream;

import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.command.LogContainerResultCallback;

/**
 * @author wkb
 *
 */
public class LogStreamContainerResultCallback extends LogContainerResultCallback {

    private ServletOutputStream outputStream;

    private OutputStreamWriter writer;

    boolean collectFrames = false;

    public LogStreamContainerResultCallback(ServletOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void onStart(Closeable stream) {
        writer = new OutputStreamWriter(outputStream);
        super.onStart(stream);
    }

    @Override
    public void onNext(Frame frame) {
        try {
            if (null != writer && null != frame) {
                writer.write(new String(frame.getPayload()));
            }
        } catch (Exception e) {}
    }

    @Override
    public void onComplete() {
        try {
            if (null != writer && outputStream != null) {
                writer.flush();
                outputStream.flush();
                writer.close();
                outputStream.close();
            }
        } catch (Exception e) {}
        super.onComplete();
    }
}
