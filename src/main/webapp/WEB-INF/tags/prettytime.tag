<%@ tag import="com.ocpsoft.pretty.time.*"  import="org.joda.time.DateTime" import="java.util.Locale"%>
<%@ attribute name="date" required="true" type="org.joda.time.DateTime" %>
  
<%
 PrettyTime p = new PrettyTime(new Locale("es"));
    String prettier = p.format(date.toDate());
 out.println(prettier);
%>