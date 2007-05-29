<?xml version="1.0" encoding="UTF-8" ?>

<!--
    Document   : Service.xsl
    Created on : May 29, 2007, 10:53 AM
    Author     : rcooper
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="text"/>
    <xsl:param name="destinationPackage">com.totsp.example</xsl:param>
    
    <!-- TODO customize transformation rules 
         syntax recommendation http://www.w3.org/TR/xslt 
    -->
    <xsl:template match="/">
        package <xsl:value-of select="concat($destinationPackage, '.client')" />;
        import com.google.gwt.user.client.rpc.RemoteService;
        <xsl:for-each select="service">
            <xsl:call-template name="service" />
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="service">
        public interface <xsl:value-of select="string(./shortName)" /> extends RemoteService{
        
        <xsl:for-each select="method" >
            <xsl:call-template name="method" />
        </xsl:for-each>
        
        }
    </xsl:template>
    
    <xsl:template name="method">
        public <xsl:for-each select="./returnType"><xsl:call-template name="type" /></xsl:for-each>
        <xsl:if test="count(./returnType) = 0">void</xsl:if>
        <xsl:value-of select="concat(' ', string(./name))"/>(
        <xsl:for-each select="./argument">
            <xsl:call-template name="type" /> <xsl:value-of select="concat( ' ', string(./name))" />
            <xsl:if test="last()!=position()">,</xsl:if>
        </xsl:for-each>);
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
            <xsl:when test="string(./package)=string(/service/package)"><xsl:value-of select="concat($destinationPackage, '.client')"/></xsl:when>
            <xsl:when test="starts-with(string(./package),concat(string(/service/package), concat('.', string(/service/shortName))))"><xsl:value-of select="concat( concat($destinationPackage, '.client'), substring( string(./package), string-length( /service/package) + 1  ) )" /></xsl:when>
            <xsl:when test="string(./package)!=string(/service/package)"><xsl:value-of select="./package" /></xsl:when>
            
        </xsl:choose>
    </xsl:template>  
    
    
    
</xsl:stylesheet>
