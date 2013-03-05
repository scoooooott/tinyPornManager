<html>
    <head>
    </head>

    <body>

    <table>
<#list movies as movie>
    <#assign trCss = (movie_index % 2 == 0)?string("even","odd")>
        <tr class="${trCss}">
            <td>${movie.name?xhtml}</td>
            <td>${movie.year}</td>
            <td>
            <#list movie.genres as genre>
                ${genre}<br/>
            </#list>
            </td>
        </tr>
</#list>
    </table>

    </body>
</html>
