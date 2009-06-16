<p><h1>${session.title}</h1></p>
<p><b>Keywords:</b>
    <#list session.keywords as keyword>
        ${keyword} 
    </#list> 
</p>
<p><h2>Speakers:</h2></p>
<#list session.speakers as speaker>
   <p>
      <#if speaker.photo??>
          <img src="${speaker.photo.uri}" alt="${speaker.name}" width="100" height="100" /><br/>
      </#if>
      <strong>Name:</strong> ${(speaker.name)!"No Name"} <#if speaker.description??>(${speaker.description})</#if>
   </p>
</#list>
<p><strong>Session state:</strong> ${session.state!""}</p>
<p><strong>Session format:</strong> ${session.format!""}</p>
<p><strong>Room:</strong> 
    <#if session.room??>${session.room.name}</#if>
</p>
<strong>Abstract:</strong><br/>
<p><#if session.lead??>${session.lead}</#if></p>
<#if !shortFormat??>
<p>
    <#if bodyText??>${bodyText}
        <#else>
            <#if session.body??>${session.body}</#if>
    </#if>
</p>
</#if>
