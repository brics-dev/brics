<%@ taglib uri="/struts-tags" prefix="s" %>

<%--
This is a catch-all jsp that only decides which of the other footer.jsp files
(IE: which template) to use and includes it.  Just a layer of abstraction here.
--%>
<s:include value="%{#systemPreferences.footerUrl}" />