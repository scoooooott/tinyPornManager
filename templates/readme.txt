
A short description how templating works :)

Please do not work on the provided ones.
They will be overwritten on each startup ;)
If you want to change/extend them, just copy them to a new .jmte file.

Templates starting with "list" are for movie lists, 
where starting with "detail" are for a single movie details page (d'oh!)

We use the "Java Minimal Template Engine", so the reference guide for the experts is located here:
http://jmte.googlecode.com/svn/trunk/doc/index.html

=================================================================================
Basics:
=================================================================================
Iterate over a List():
${foreach LISTNAME as VARIABLE}
    ${VARIABLE}   <!-- print out variable -->
    [...] do more
${end}

Display a String variable
    ${StringName}

Display a Date variable
    ${DateVariable}
    ${DateVariable;date(yyyy-MM-dd)}

=================================================================================
Eg: to print some movie details
${foreach movies movie}
    ${movie.name} - ${movie.year}
${end}

Eg: to print some movie details, with genres
${foreach movies movie}
    ${movie.name}
    ${foreach movie.genres genre , }       // " , " comma is used here as genre seperator
        ${genre}
    ${end}
${end}
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
