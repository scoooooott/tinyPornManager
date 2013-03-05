sep=,
<#-- specify seperator character in FIRST LINE
US defaults to , whereas eg German defaults to ; -->

<#list movies as movie>
${movie.name},${movie.year},${movie.path}
</#list>
