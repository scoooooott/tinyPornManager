
A short description how templating works :)
We use the "Java Minimal Template Engine", so the reference guide for the experts is located here:
http://jmte.googlecode.com/svn/trunk/doc/index.html

Please do not work on the provided files.
They will be overwritten on each startup ;)
If you want to change/extend them, just copy them to a new directory including the altered template.conf.

If it's a cool template, let us know so we could redistribute it :)

Templates starting with "list" are for movie listings, 
where starting with "detail" are for a listing including single movie details pages (d'oh!)

=================================================================================
Configuration:
=================================================================================
Each template has to be in its own directory including a template.conf file containing:
name=<name of template>                         (needed for displaying in UI)
type={movie}                                    (needed for deciding between movie templates and others)
list=<path to list template>                    (needed - each template needs at least a list file)
detail=<path to detail template>                (optional - if the template has detail pages for each movie)
extension={html|xml|csv}                        (optional - filetype; if nothing specified here, tmm will take html)

The exporter will create a index.html (or movielist.csv/movielist.xml) for movie listings and 
movies/<export name of movie>.html for detail pages (<export name of movie> will build the way you defined it in the 
renamer settings or <moviename> if nothing is defined there)

Beside the 2 (or 3 in case of a detail template) file there can be other files/directories in the template directory, 
which will be copied to the export destination (e.g. stylesheets, images, ...)

=================================================================================
Basics:
=================================================================================
Comments in template - will not be rendered:
${-- this is a comment}

Iterate over a List():
${foreach LISTNAME as VARIABLE}
    ${VARIABLE}   <!-- print out variable -->
    [...] do more
${end}

Display a String variable
    ${StringName}

Display a Date variable
    ${DateVariable}
    ${DateVariable;date(dd.MM.yyyy HH:mm:ss Z)}  // or whatever pattern you like

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

Movie:
Date                  dateAdded
List<MediaFile>       mediaFiles
List<MediaGenres>     genres
List<MediaTrailer>    trailer
List<MovieCast>       actors        
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

MovieCast:
String                character
String                name

MediaFile:
String                path
String                filename
String                filesize
String                videoCodec      
String                audioCodec      
String                audioChannels   
String                containerFormat  
String                videoFormat      
String                exactVideoFormat 
int                   videoWidth       
int                   videoHeight      
int                   overallBitRate   
int                   duration         

MediaTrailer:
String                name
String                url
String                provider