<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
    <head>
    </head>

    <body>
    <style type="text/css">
        .odd { color:red; }
        .even { color:blue; }
    </style>
    <table border="1">
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
