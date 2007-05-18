<?xml version="1.0" encoding="UTF-8" ?>

<!--
    Document   : DTO.xsl
    Created on : May 17, 2007, 3:40 PM
    Author     : rcooper
    Description:
        This template is meant to do transformations to DTOs from XML spec files for beans.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="text"/>
    <xsl:param name="propertyChangeSupport">yes</xsl:param>
    <xsl:param name="gettersAndSetters">yes</xsl:param>
    <xsl:param name="destinationPackage">com.totsp.example.client</xsl:param>
    
    <xsl:template match="/class">
        package <xsl:value-of select="$destinationPackage"/>;
        import com.google.gwt.user.client.rpc.IsSerializable;
        <xsl:if test="$propertyChangeSupport='yes'">
            import java.beans.PropertyChangeSupport;
            import java.beans.PropertyChangeListener;
        </xsl:if>
        
        public class <xsl:value-of select="./shortName" /> 
        <xsl:choose>
            <xsl:when test="count(./extends)&gt;0 and string(./extends/package)=string(/class/package)">
                extends <xsl:value-of select="./extends/shortName" />
            </xsl:when>
            <xsl:when test="count(./extends)&gt;0 and string(./extends/package)!=string(/class/package)">
                extends <xsl:value-of select="./extends/package"/>.<xsl:value-of select="./extends/shortName" />
            </xsl:when>
        </xsl:choose>
        implements IsSerializable{
        <xsl:if test="$propertyChangeSupport='yes'">
            <xsl:call-template name="propertyChangeSupport" />
        </xsl:if>
        
        
        }
    </xsl:template>
    <xsl:template name="propertyChangeSupport">
        private PropertyChangeSupport __propertyChangeSupport__ = new PropertyChangeSupport(this);
        public void addPropertyChangeListener(PropertyChangeListener l) {
        __propertyChangeSupport__.addPropertyChangeListener(l);
        }
        public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
        __propertyChangeSupport__.addPropertyChangeListener(propertyName, l);
        }
        public PropertyChangeListener[] getPropertyChangeListeners() {
        return __propertyChangeSupport__.getPropertyChangeListeners();
        }
        public void removePropertyChangeListener(PropertyChangeListener l) {
        __propertyChangeSupport__.removePropertyChangeListener(l);
        }
        
        public void removePropertyChangeListener(String propertyName,
        PropertyChangeListener l) {
        __propertyChangeSupport__.removePropertyChangeListener(propertyName, l);
        }
        <xsl:for-each select="./property">
            <xsl:call-template name="property" />
        </xsl:for-each>
    </xsl:template>
    <xsl:template name="property">
        <xsl:if test="count(./parameterType)!=0">
            /**
             * @gwt.typeArgs &lt;<xsl:for-each select="./parameterType"><xsl:call-template name="packageName" />.<xsl:value-of select="./shortName" /><xsl:if test="last()!=position()">,</xsl:if></xsl:for-each>&gt;
             */  
        </xsl:if>
        <xsl:choose>
            <xsl:when test="$gettersAndSetters='yes' or $propertyChangeSupport='yes'">private </xsl:when>
            <xsl:otherwise>public </xsl:otherwise>
        </xsl:choose>
        <xsl:call-template name="type" />
        <xsl:value-of select="concat(' ',string(./name))" />;
        <xsl:if test="$gettersAndSetters='yes'or $propertyChangeSupport='yes'">
            public <xsl:call-template name="type" /> 
            <xsl:choose>
                <xsl:when test="string(./shortName)='boolean' or string(./shortName)='Boolean'"> is</xsl:when>
                <xsl:otherwise> get</xsl:otherwise>
            </xsl:choose>
            <xsl:call-template name="methodName" />(){
            return this.<xsl:value-of select="./name" />;
            }
            
            public void set<xsl:call-template name="methodName" />( <xsl:call-template name="type" />  value ){
            <xsl:if test="$propertyChangeSupport='yes'">
                <xsl:call-template name="type" /> old = this.<xsl:value-of select="./name" />;
            </xsl:if>
            this.<xsl:value-of select="name" /> = value;
            <xsl:if test="$propertyChangeSupport='yes'">
                __propertyChangeSupport__.firePropertyChange( "<xsl:value-of select="./name" />", 
                <xsl:call-template name="box"><xsl:with-param name="var">old</xsl:with-param></xsl:call-template>,
                <xsl:call-template name="box"><xsl:with-param name="var">value</xsl:with-param></xsl:call-template>);
            </xsl:if>   
            }
        </xsl:if>
        
        
    </xsl:template>
    
    <xsl:template name="box">
        <xsl:param name="var" />
        <xsl:choose>
            <xsl:when test="string(./shortName)='int' and count(./arrayDepth)=0">
                new Integer( <xsl:value-of select="$var" /> )
            </xsl:when>
            <xsl:when test="string(./shortName)='long' and count(./arrayDepth)=0">
                new Long( <xsl:value-of select="$var" /> )
            </xsl:when>
            <xsl:when test="string(./shortName)='float' and count(./arrayDepth)=0">
                new Float( <xsl:value-of select="$var" /> )
            </xsl:when>
            <xsl:when test="string(./shortName)='double' and count(./arrayDepth)=0">
                new Double( <xsl:value-of select="$var" /> )
            </xsl:when>
            <xsl:when test="string(./shortName)='char' and count(./arrayDepth)=0">
                new Character( <xsl:value-of select="$var" /> )
            </xsl:when>
            <xsl:when test="string(./shortName)='byte' and count(./arrayDepth)=0">
                new Byte( <xsl:value-of select="$var" /> )
            </xsl:when>
            <xsl:when test="string(./shortName)='boolean' and count(./arrayDepth)=0">
                new Boolean( <xsl:value-of select="$var" /> )
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$var" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="type">
        <xsl:choose>
            <xsl:when test="string(./shortName)='int' or
                          string(./shortName)='long' or
                          string(./shortName)='float' or
                          string(./shortName)='double' or
                          string(./shortName)='char' or
                          string(./shortName)='byte' or
                          string(./shortName)='boolean'">
                              <xsl:value-of select="./shortName" />
                </xsl:when>
                
            <xsl:otherwise>
                <xsl:call-template name="packageName" />.<xsl:value-of select="./shortName" /><xsl:call-template name="array" />
            </xsl:otherwise>
        </xsl:choose>
        
    </xsl:template>
    <xsl:template name="methodName">
        <xsl:value-of select="translate(substring(string(./name),1,1),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
        <xsl:value-of select="substring(string(./name), 2)" />
    </xsl:template>
    <xsl:template name="array">
        <xsl:if test="number(./arrayDepth)&gt;0">
            <xsl:call-template name="arrayLoop">
                <xsl:with-param name="count">
                    <xsl:value-of select="./arrayDepth" />
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if> 
    </xsl:template>
    <xsl:template name="arrayLoop"><xsl:param name="count"  />[]<xsl:if test="$count&gt;1">
            <xsl:call-template name="arrayLoop">
                <xsl:with-param name="count">
                    <xsl:value-of select="number($count)- 1" />
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if>
        
    </xsl:template>
    <xsl:template name="packageName" ><xsl:choose>
            <xsl:when test="string(./package)=string(/class/package)"><xsl:value-of select="$destinationPackage"/></xsl:when>
            <xsl:when test="string(./package)!=string(/class/package)"><xsl:value-of select="./package" /></xsl:when>
    </xsl:choose></xsl:template>
</xsl:stylesheet>
