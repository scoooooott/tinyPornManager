
A short description how templating works :)

Please do not work on the provided ones.
They will be overwritten on each startup ;)
If you want to change/extend them, just copy them to a new .ftl file.

Templates starting with "list" are for movie lists, 
where starting with "detail" are for a single movie details page (d'oh!)

We use freemarker, so the reference guide for the experts is located here:
http://freemarker.sourceforge.net/docs/ref.html

=================================================================================
Basics:
=================================================================================
Iterate over a List():
<#list LISTNAME as VARIABLE>
    ${VARIABLE}   <!-- print out variable -->
    [...] do more
</#list>

Display a String variable
    ${StringName}

Display a Date variable
    ${DateVariable?date}
    ${DateVariable?time}
    ${DateVariable?datetime}
    ${DateVariable?string.<format>}

For encoding of values, use the following:
    ${movie.name?html}
    ${movie.name?xml}
    ${movie.name?xhtml}

=================================================================================
Eg: to print some movie details
<#list movies as movie>
    ${movie.name} - ${movie.year}
</#list>

Eg: to print some movie details, with genres
<#list movies as movie>
    ${movie.name}
    <#list movie.genres as genre>
        - ${genre}
    </#list>
</#list>
=================================================================================


Following variables can be used:
(as part of movie variable)

Date                  dateAdded
List<MediaFile>       mediaFiles
List<MediaGenres>     genres
List<MediaTrailer>    trailer
List<MovieCast>       cast
List<String>          extraThumbs
List<String>          tags
MovieSet              movieSet;
String                dataSource
String                director
String                fanart
String                fanartUrl
String                imdbId
String                name
String                nameSortable
String                nfoFilename
String                originalName
String                overview
String                path
String                poster
String                posterUrl
String                productionCompany
String                sortTitle
String                spokenLanguages
String                tagline
String                writer
String                year
boolean               duplicate
boolean               isDisc
boolean               scraped
boolean               watched
float                 rating
int                   runtime
int                   tmdbId
int                   votes
