package edu.cpp.iipl.crawlers.amazon.util;

/**
 * A very simple logger
 *
 * Created by xing on 12/24/15.
 */
public class Logger {

    private boolean isVerbose = false;

    public boolean isVerbose() {
        return isVerbose;
    }

    public void setVerbose(boolean verbose) {
        isVerbose = verbose;
    }

    public void info(Object x) {
        if (isVerbose)
            System.out.println(x);
    }

    public void warn(Object x) {
        System.err.println(x);
    }
}
