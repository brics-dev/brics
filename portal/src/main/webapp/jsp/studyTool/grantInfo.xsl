<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="/">
		<div id="main-content">
			<h3>Grant Information</h3>

			<xsl:for-each select="listWrapper/list/listItem">
				<div class="form-output">
					<div class="label">Title :</div>
					<div class="readonly-text">
						<xsl:value-of select="grantTitle" />
					</div>
				</div>
				<div class="form-output">
					<div class="label">Abstract :</div>
					<div class="readonly-text">
						<xsl:value-of select="grantAbstract" />
					</div>
				</div>
				<div class="form-output">
					<div class="label">PI :</div>
					<div class="readonly-text">
						<xsl:value-of select="piLastName" />
						,
						<xsl:value-of select="piFirstName" />
					</div>
				</div>
			</xsl:for-each>

		</div>
	</xsl:template>

</xsl:stylesheet> 
