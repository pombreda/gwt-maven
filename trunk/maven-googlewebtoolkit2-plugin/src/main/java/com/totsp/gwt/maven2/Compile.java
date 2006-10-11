package com.totsp.gwt.maven2;


import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Runs the GWT Java to Javascript Compiler
 *
 * @goal compile
 *
 * @phase compile
 */
public class Compile
        extends GWTOperation {
    private static final String COMPILER_CLASS_NAME = "com.google.gwt.dev.GWTCompiler";
        
    /**
     * Location of the source files.
     *
     * @parameter
     * @required
     */
    private List sourceDirectories;
    
    public void execute()
    throws MojoExecutionException {
        this.runMainClass( Compile.COMPILER_CLASS_NAME );
        
    }
    
    protected void runMainClass( final String execute) throws MojoExecutionException{
        if (getLog().isDebugEnabled()) {
            getLog().debug("CompileMojo#execute()");
        }
        
        ClassLoader loader = getClassLoader();
        
        
        Class compiler = null;
        try {
            compiler = loader.loadClass(execute);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Could not find "+execute, e);
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("  Found class:" + compiler);
        }
        
        final Method main;
        try {
            main = compiler.getMethod("main", String[].class);
        } catch (SecurityException e) {
            throw new MojoExecutionException("Permission not granted for reflection.", e);
        } catch (NoSuchMethodException e) {
            throw new MojoExecutionException("Could not find GWTCompiler#main(String[]).", e);
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("  Found method:" + main);
        }
        
        // TODO : what other options are there?
        final List<String> args = new LinkedList<String>();
        args.add("-out");
        args.add(this.googleWebToolkitOutputDirectory.getAbsolutePath() );
        args.add(this.googleWebToolkitCompileTarget );
        if (getLog().isDebugEnabled()) {
            getLog().debug("  Invoking main with" + args);
        }
        
        // TODO : can we have the gwt source directory already in the classpath?
        Runnable compile = new Runnable() {
            public void run() {
                try {
                    main.invoke(null, new Object[] {
                        args.toArray(new String[args.size()])
                    });
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("This shouldn't happen.", e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Permission not granted for reflection.", e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException("GWTCompiler#main(String[]) failed.", e);
                }
            }
        };
        
        // TODO : we can just swap ContextClassLoader in this block
        Thread compileThread = new Thread(compile);
        compileThread.setContextClassLoader(loader);
        compileThread.start();
        try {
            compileThread.join();
        } catch (InterruptedException e) {
            throw new MojoExecutionException("Compiler thread stopped.", e);
        }
    }
    
    
    protected ClassLoader getClassLoader() throws MojoExecutionException {
        URLClassLoader myClassLoader = (URLClassLoader)
        getClass().getClassLoader();
        
        URL[] originalUrls = myClassLoader.getURLs();
        URL[] urls = new URL[originalUrls.length + sourceDirectories.size()];
        for (int index = 0; index < originalUrls.length; ++index) {
            try {
                String url = originalUrls[index].toExternalForm();
                urls[index] = new
                        File(url.substring("file:".length())).toURI().toURL();
            } catch (MalformedURLException e) {
                throw new MojoExecutionException("MalformedURLException", e);
            }
        }
        
        int count = 0;
        for(Iterator itr = sourceDirectories.iterator(); itr.hasNext();
        count++ ){
            try {
                File sourceDirectory = new File(itr.next().toString());
                urls[originalUrls.length + count] = sourceDirectory.toURL();
            } catch (MalformedURLException e) {
                throw new MojoExecutionException("MalformedURLException", e);
            }
        }
        
        return new URLClassLoader(urls, myClassLoader.getParent());
    }
}
