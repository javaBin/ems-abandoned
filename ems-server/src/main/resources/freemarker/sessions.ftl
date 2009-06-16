<html>
	<head>
		<title>All sessions</title>
	</head>
	<body>
		<table>
			<#list sessions as session>
			 <tr><td>
			    <#assign shortFormat = true>
                <#include "single_session.ftl">
             </td></tr>
			</#list>
		</table>
	</body>
</html>
