/*
 * (C) Copyright DeadSix27 Tesch 2013-2014
 * 
 * Contact: contact@dead6.eu
 * Website: www.Dead6.eu / www.moreIT.eu
 * IRC: irc.Dead6.eu
 * myXBMC HTML Generator
 * 
 * This product is not affiliated with XBMC, every use of naming is just for reference to ensure the right usage of this Program.
 */
lastHilight = "";
function GetUrlValue(VarSearch) {
    var SearchString = window.location.search.substring(1);
    var VariableArray = SearchString.split('&');
    for (var i = 0; i < VariableArray.length; i++) {
        var KeyValuePair = VariableArray[i].split('=');
        if (KeyValuePair[0] == VarSearch) {
            return KeyValuePair[1];
        }
    }
    return "none";
}
function showPlot(divID) {
    $("#plot_" + divID).fadeIn();
    $("#plot_" + divID + "_icon").fadeOut();
}
function hidePlot(divID) {
    $("#plot_" + divID).fadeOut();
    $("#plot_" + divID + "_icon").fadeIn();
}
function closeTrailer(divID) {
    $("#video_" + divID).html("");
    $("#closeButton_" + divID).fadeOut();
    $("#overlay").animate({ height: '1%', opacity: 0 }, 500, function () {
        $("#overlay").hide();
    });
}
function fun() {
    var x = 1
    $('.aniContainer').each(function () {
        if (x != 1) {
            $(this).hide();
        }
        else { x = 0 }
    });
    openTrailer('ki3SYtylids', $('.aniContainer').first().attr('id').substring(7, $('.aniContainer').first().attr('id').length), '71s');
    $('.aniContainer').first().css('max-width', '');
    $('.aniContainer').first().css('margin-bottom', '200px');
    $('.aniContainer').first().css('max-height', '');
    $('.aniContainer').first().css('overflow', 'visible');
    $('.closeIcon').first().hide();
    $('iframe').animate({ width: '1260px', height: '735px' }, 2000, function () {
        var y = $(window).scrollTop();
        $(window).scrollTop($(window).scrollTop() + 350);
    });
}
function funBarrelRoll() {
    $('html').animate({ borderSpacing: -360 }, {
        step: function (now, fx) {
            $(this).css('-webkit-transform', 'rotate(' + now + 'deg)');
            $(this).css('-moz-transform', 'rotate(' + now + 'deg)');
            $(this).css('-ms-transform', 'rotate(' + now + 'deg)');
            $(this).css('-o-transform', 'rotate(' + now + 'deg)');
            $(this).css('transform', 'rotate(' + now + 'deg)');
        },
        duration: 1000
    }, 'linear');
}
function funBarrelRoll2() {
    $('.posterImage').animate({ borderSpacing: -360 }, {
        step: function (now, fx) {
            $(this).css('-webkit-transform', 'rotate(' + now + 'deg)');
            $(this).css('-moz-transform', 'rotate(' + now + 'deg)');
            $(this).css('-ms-transform', 'rotate(' + now + 'deg)');
            $(this).css('-o-transform', 'rotate(' + now + 'deg)');
            $(this).css('transform', 'rotate(' + now + 'deg)');
        },
        duration: 2500
    }, 'linear');
    $('.infoBackTitleSub').animate({ borderSpacing: 360 }, {
        step: function (now, fx) {
            $(this).css('-webkit-transform', 'rotate(' + now + 'deg)');
            $(this).css('-moz-transform', 'rotate(' + now + 'deg)');
            $(this).css('-ms-transform', 'rotate(' + now + 'deg)');
            $(this).css('-o-transform', 'rotate(' + now + 'deg)');
            $(this).css('transform', 'rotate(' + now + 'deg)');
        },
        duration: 2500
    }, 'linear');
    $('.infoBackTitle').animate({ borderSpacing: -360 }, {
        step: function (now, fx) {
            $(this).css('-webkit-transform', 'rotate(' + now + 'deg)');
            $(this).css('-moz-transform', 'rotate(' + now + 'deg)');
            $(this).css('-ms-transform', 'rotate(' + now + 'deg)');
            $(this).css('-o-transform', 'rotate(' + now + 'deg)');
            $(this).css('transform', 'rotate(' + now + 'deg)');
        },
        duration: 2500
    }, 'linear');
    $('.pagesContainer').animate({ borderSpacing: -360 }, {
        step: function (now, fx) {
            $(this).css('-webkit-transform', 'rotateY(' + now + 'deg)');
            $(this).css('-moz-transform', 'rotateY(' + now + 'deg)');
            $(this).css('-ms-transform', 'rotateY(' + now + 'deg)');
            $(this).css('-o-transform', 'rotateY(' + now + 'deg)');
            $(this).css('transform', 'rotateY(' + now + 'deg)');
        },
        duration: 2500
    }, 'linear');
}
function openTrailer(youtubeID, divID, start) {
    if (youtubeID.substring(0, 6) == "daily_") {
        youtubeID = youtubeID.substring(6, youtubeID.length);
        if (start != "") { start = "&start=" + start; }
        $("#video_" + divID).html("<iframe src=\"http://www.dailymotion.com/embed/video/" + youtubeID + "?autoPlay=1&forcedQuality=hd720&wmode=transparent" + start + "\"></iframe>");
    }
    else {
        if (start != "") { start = "#t=" + start; }
        $("#video_" + divID).html("<iframe src=\"http://www.youtube-nocookie.com/embed/" + youtubeID + "?autoplay=1&wmode=transparent&iv_load_policy=3" + start + "\"></iframe>");
    }
    $("#closeButton_" + divID).fadeIn();
    $("#overlay").show().animate({ height: '100%', opacity: 0.8 }, 500);
}
function clearHilight() {
    $(lastHilight).css('box-shadow', '');
    $(lastHilight).css('border', '');
    $(lastHilight).css('border-radius', '');
}
function checkHash() {
    var urlHash = window.location.hash;
    lastHilight = urlHash;
    if ((urlHash != "") && (urlHash != " ")) {
        $(urlHash).css('box-shadow', '0 0 25px #0DF3FF');
        //$(urlHash).delay(1000).css('box-shadow','0 0 25px #0DF3FF');
        $(urlHash).css('border', '0 px solid');
        $(urlHash).css('border-radius', '11px');
        $(urlHash).delay(500).effect("shake", { direction: 'left', times: 2, distance: 20 }, 500);
    }
}
function startUP() {
    checkHash();
    $("img.lazyLoad").lazyload()
    $("body").click(function () {
        $("#search_result").animate({ height: '1px' }, 100, function () {
            $("#search_result").hide();
            $("#search_result").html("");
        });
    });
}
function searchVideo(searchStr, type, htmlFn, HtmlExt, evt) {
    var xmlFile = ""
    var nodeName = "";
    if (type == "t") {
        xmlFile = "tvShowPageIndex.xml";
        nodeName = "tvshow";
    }
    if (type == "m") {
        xmlFile = "moviePageIndex.xml";
        nodeName = "movie";
    }
    var pressedKey = "";
    if (typeof event != 'undefined') {
        if (event.keyCode) {
            pressedKey = String.fromCharCode(event.keyCode);
        }
    }
    else if (evt) {
        pressedKey = String.fromCharCode(evt.which);
    }
    searchStr = searchStr + pressedKey.toLowerCase();
    if ((searchStr != " ") && (searchStr != "")) {
        //searchStr = searchStr.substring(0, searchStr.length);
        $.ajax({
            type: "GET",
            url: xmlFile,
            cache: false,
            dataType: "xml",
            success: function (xml) {
                var $xml = $(xml);
                var result = "";
                //var stop = false;
                var output = "";
                var maxAmmount = 13;
                var ammount = 0;
                var lastResult = "";
                //result = $xml.find('tvshow[name="'+searchStr+'"]').text();
                $xml.find(nodeName).each(
                    function (i) {
                        var $car = $(this);
                        var $name = $car.attr('name');
                        var $videoID = $car.attr('id');
                        var $page = $car.text();
                        if ($name.toLowerCase().indexOf(searchStr) != -1) {
                            maxAmmount = maxAmmount - 1;
                            if (maxAmmount > 1) {
                                result = $videoID;
                                ammount = ammount + 1;
                                //if (stop!=true){
                                //console.log(i + " -> " + $name);
                                if ($page == "0") {
                                    $page = "";
                                } else {
                                    $page = "_" + $page;
                                }
                                if (type == "m") {
                                    output += '<a href="./' + htmlFn + $page + HtmlExt + '#movie_' + $videoID + '">' + $name + '</a></br>';
                                }
                                if (type == "t") {
                                    output += '<a href="./' + htmlFn + $page + HtmlExt + '#tvshow_' + $videoID + '">' + $name + '</a></br>';
                                }
                                //document.getElementById('result').innerHTML+=$name + " -> on Page Nr: " + $page + "</br>";
                                //stop=true;
                                //}
                            }
                            lastResult = $videoID;
                        } else {
                            lastResult = "";
                        }
                    });
                //console.log(result);
                if ((result == "") || (result == " ") || (lastResult = "")) {
                    $("#search_result").animate({ height: '1px' }, 500, function () {
                        $("#search_result").hide();
                        $("#search_result").html("");
                    });
                } else {
                    $("#search_result").html(output);
                    $("#search_result").show().animate({ height: ((ammount * 13) + 5) + "px" }, 100);
                    //document.getElementById('search_result').style.height = (ammount * 13) + 5;
                }
                checkHash();
            },
        });
    }
}






