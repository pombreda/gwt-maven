<?xml version="1.0" encoding="UTF-8" ?>
	<!--
		Document : Service.xsl Created on : May 29, 2007, 10:53 AM Author :
		rcooper Description: Purpose of transformation follows.
	-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	<xsl:output method="text" />
	<xsl:param name="destinationPackage">
		com.totsp.example
	</xsl:param>
	<!--
		TODO customize transformation rules syntax recommendation
		http://www.w3.org/TR/xslt
	-->
	<xsl:template match="/">
		package
		<xsl:value-of select="concat($destinationPackage, '.server')" />
		; import com.google.gwt.user.client.rpc.RemoteService; import
		com.google.gwt.user.server.rpc.RemoteServiceServlet; import
		com.totsp.gwt.beans.server.BeanMapping; import
		com.totsp.gwt.beans.server.MappingException; import
		java.beans.IntrospectionException; import
		java.lang.reflect.InvocationTargetException; import
		javax.servlet.ServletException;
		<xsl:for-each select="service">
			<xsl:call-template name="service" />
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="service">
		public class
		<xsl:value-of select="string(./shortName)" />
		Servlet extends RemoteServiceServlet implements
		<xsl:value-of
			select="concat(concat($destinationPackage, '.client.'), string(./shortName))" />
		{ private
		<xsl:value-of select="string(./implementation)" />
		service = new
		<xsl:value-of select="string(./implementation)" />
		(); private static final java.util.Properties mappings = new
		java.util.Properties(); static { mappings.put( "
		<xsl:value-of select="string(./package)" />
		.*", "
		<xsl:value-of select="$destinationPackage" />
		.*" );
		<xsl:for-each select="mapping">
			mappings.put( "
			<xsl:value-of select="string(./from)" />
			.*", "
			<xsl:value-of select="string(./to)" />
			.*" />);
		</xsl:for-each>
		}
		<xsl:for-each select="method">
			<xsl:call-template name="method" />
		</xsl:for-each>
		}
	</xsl:template>
	<xsl:template name="method">
		public
		<xsl:for-each select="./returnType">
			<xsl:call-template name="type" />
		</xsl:for-each>
		<xsl:if test="count(./returnType) = 0">
			void
		</xsl:if>
		<xsl:value-of select="concat(' ', string(./name))" />
		(
		<xsl:for-each select="./argument">
			<xsl:call-template name="type" />
			<xsl:value-of select="concat( ' ', string(./name))" />
			<xsl:if test="last()!=position()">
				,
			</xsl:if>
		</xsl:for-each>
		){ if( service instanceof com.totsp.gwt.beans.server.HttpService ){
		com.totsp.gwt.beans.server.HttpService httpService =
		(com.totsp.gwt.beans.server.HttpService) service;
		httpService.setThreadLocal( this.getThreadLocalRequest(),
		this.getThreadLocalResponse() ); } try {
		<xsl:if test="count(./returnType) &gt; 0">
			<xsl:for-each select="./returnType">
				<xsl:call-template name="type" />
				returnValue = (
				<xsl:call-template name="type" />
				)
			</xsl:for-each>
		</xsl:if>
		<xsl:if
			test="count(./returnType) &gt; 0 and
                    string(./returnType/shortName) != 'int' and
                    string(./returnType/shortName) != 'long' and
                    string(./returnType/shortName) != 'float' and
                    string(./returnType/shortName) != 'double' and
                    string(./returnType/shortName) != 'char' and
                    string(./returnType/shortName) != 'byte' and
                    string(./returnType/shortName) != 'boolean' ">
			BeanMapping.convert( mappings,
		</xsl:if>
		service.
		<xsl:value-of select="./name" />
		(
		<xsl:for-each select="./argument">
			<xsl:if
				test="string(./shortName)!='int' and
                        string(./shortName)!='long' and
                        string(./shortName)!='float' and
                        string(./shortName)!='double' and
                        string(./shortName)!='char' and
                        string(./shortName)!='byte' and
                        string(./shortName)!='boolean'">
				(
				<xsl:call-template name="rawType" />
				) BeanMapping.convert( mappings,
			</xsl:if>
			<xsl:value-of select="string(./name)" />
			<xsl:if
				test="string(./shortName)!='int' and
                        string(./shortName)!='long' and
                        string(./shortName)!='float' and
                        string(./shortName)!='double' and
                        string(./shortName)!='char' and
                        string(./shortName)!='byte' and
                        string(./shortName)!='boolean'">
				)
			</xsl:if>
			<xsl:if test="last()!=position()">
				,
			</xsl:if>
		</xsl:for-each>
		<xsl:if
			test="count(./returnType) &gt; 0 and
                    string(./returnType/shortName)!='int' and
                    string(./returnType/shortName)!='long' and
                    string(./returnType/shortName)!='float' and
                    string(./returnType/shortName)!='double' and
                    string(./returnType/shortName)!='char' and
                    string(./returnType/shortName)!='byte' and
                string(./returnType/shortName)!='boolean'">
			)
		</xsl:if>
		);
		<xsl:if test="count(./returnType) &gt; 0">
			return returnValue;
		</xsl:if>
		<xsl:for-each select="thrown">
			} catch(
			<xsl:call-template name="rawType" />
			e ){ this.log( "Service exception" , e );
			<xsl:call-template name="type" />
			exception = new
			<xsl:call-template name="type" />
			( e.getMessage() );
		</xsl:for-each>
		} catch( IntrospectionException e ){ this.log( "Exception in
		conversion" , e ); throw new RuntimeException( e ); } catch(
		ClassNotFoundException e ){ this.log( "Exception in conversion" , e );
		throw new RuntimeException( e ); } catch( InstantiationException e ){
		this.log( "Exception in conversion" , e ); throw new RuntimeException(
		e ); } catch( IllegalAccessException e ){ this.log( "Exception in
		conversion" , e ); throw new RuntimeException( e ); } catch(
		InvocationTargetException e ){ this.log( "Exception in conversion" , e
		); throw new RuntimeException( e ); } catch( MappingException e ){
		this.log( "Exception in conversion" , e ); throw new RuntimeException(
		e ); } finally { if( service instanceof
		com.totsp.gwt.beans.server.HttpService ){
		com.totsp.gwt.beans.server.HttpService httpService =
		(com.totsp.gwt.beans.server.HttpService) service;
		httpService.unsetThreadLocal(); } } throw new RuntimeException("How
		did you get here?"); }
	</xsl:template>
	<xsl:template name="type">
		<xsl:choose>
			<xsl:when
				test="string(./shortName)='int' or
                      string(./shortName)='long' or
                      string(./shortName)='float' or
                      string(./shortName)='double' or
                      string(./shortName)='char' or
                      string(./shortName)='byte' or
            string(./shortName)='boolean'">
				<xsl:value-of select="./shortName" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="packageName" />
				.
				<xsl:value-of select="./shortName" />
				<xsl:call-template name="array" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="rawType">
		<xsl:choose>
			<xsl:when
				test="string(./shortName)='int' or
                      string(./shortName)='long' or
                      string(./shortName)='float' or
                      string(./shortName)='double' or
                      string(./shortName)='char' or
                      string(./shortName)='byte' or
            string(./shortName)='boolean'">
				<xsl:value-of select="./shortName" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="string(./package)" />
				.
				<xsl:value-of select="./shortName" />
				<xsl:call-template name="array" />
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
	<xsl:template name="arrayLoop">
		<xsl:param name="count" />
		[]
		<xsl:if test="$count&gt;1">
			<xsl:call-template name="arrayLoop">
				<xsl:with-param name="count">
					<xsl:value-of select="number($count)- 1" />
				</xsl:with-param>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	<xsl:template name="packageName">
		<xsl:choose>
			<xsl:when test="string(./package)=string(/service/package)">
				<xsl:value-of select="concat($destinationPackage, '.client')" />
			</xsl:when>
			<xsl:when
				test="starts-with(string(./package),concat(string(/service/package), concat('.', string(/service/shortName))))">
				<xsl:value-of
					select="concat( concat($destinationPackage, '.client'), substring( string(./package), string-length( /service/package) + 1  ) )" />
			</xsl:when>
			<xsl:when test="string(./package)!=string(/service/package)">
				<xsl:value-of select="./package" />
			</xsl:when>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>