/*
 * FormGenerator.java
 *
 * Created on April 5, 2007, 3:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.totsp.mavenplugin.gwt.support.beans;



import java.io.File;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 *
 * @author rcooper
 */
public class FormGenerator {
    
    static HashSet<String> i18nTokens = new HashSet<String>();
    public static void main(String[] args){
        ///String destinationFolder = args[0];
        ///String destinationPackage = args[1];
        ///String modelPackage = args[2];
        StringTokenizer rootBeans = new StringTokenizer(args[3], ",");
        while( rootBeans.hasMoreTokens() ){
            
        }
    }
    public static void writeForm(File packageFolder, String modelPackage, String destinationPackage, Bean bean){
        StringBuffer sb = new StringBuffer();
        String className = bean.clazz.getSimpleName()+"Edit";
        String instanceName = bean.clazz.getSimpleName().substring(0,0).toLowerCase()+bean.clazz.getSimpleName().substring(1);
        sb.append( "package "); sb.append(destinationPackage); sb.append(";\n");
        sb.append( "import "); sb.append(modelPackage); sb.append("."); sb.append( bean.clazz.getSimpleName()); sb.append(";\n");
        sb.append( "import com.google.gwt.user.client.ui.FlexTable;\n");
        sb.append( "import java.beans.PropertyChangeListener;\n");
        sb.append( "public class "); sb.append(className); sb.append(" extends FlexTable {\n");
        sb.append( "private "+ bean.clazz.getSimpleName() + " "+ instanceName+";\n");
        sb.append( "private PropertyChangeListener[] listeners = new PropertyChangeListner["+bean.properties.size()+"];\n");
        sb.append( "public " + className+"( "+bean.clazz.getSimpleName()+" "+instanceName+") {\n");
        sb.append( "\tthis." + instanceName+" = "+instanceName+";\n" );
        sb.append( "}\n");
        sb.append( "protected void onAttach(){ \n");
    }
}
