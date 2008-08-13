<?xml version="1.0" encoding="UTF-8" ?>
	<!--
		Document : Exception.xsl Created on : May 29, 2007, 4:13 PM Author :
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
	<xsl:template match="/exception">
		package
		<xsl:value-of select="$destinationPackage" />
		.client; public class
		<xsl:value-of select="string(./shortName)" />
		extends com.google.gwt.user.client.rpc.SerializableException { public
		<xsl:value-of select="string(./shortName)" />
		(String message) { super( message ); } }
	</xsl:template>
</xsl:stylesheet>