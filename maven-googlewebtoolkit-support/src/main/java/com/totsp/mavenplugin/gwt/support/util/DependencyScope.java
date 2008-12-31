package com.totsp.mavenplugin.gwt.support.util;

// this was an inner class on BuildClasspathUtil - but Maven chokes on that with a Qdox parse exception?
public enum DependencyScope {
    COMPILE, RUNTIME, TEST
}