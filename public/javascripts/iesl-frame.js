// enable Bootstrap tooltips
function fixTooltip(selector) {
    $(selector + " a").tooltip({
        'selector': '',
        'placement': 'top'
    });
    $(selector + " .withtooltip").tooltip({
        'selector': '',
        'placement': 'top'
    });
    //console.log(selector + " a");
};

function expandInit() {
    var reducedHeight = $(this).height();
    $(this).css('height', 'auto');
    var fullHeight = $(this).height();
    $(this).height(reducedHeight);

    $(this).data('reducedHeight', reducedHeight);
    $(this).data('fullHeight', fullHeight);
};

function expandOnClick() {
    readmore = $(this).attr('read-more')
    readless = $(this).attr('read-less')
    
    $(this).animate({height: $(this).height() == $(this).data('reducedHeight') ? $(this).data('fullHeight') : $(this).data('reducedHeight')}, 200);
    $(this).toggleClass('cursor-down cursor-up', 200);
    $(this).prop('title', function (idx, oldAttr) {
        console.log("oldAttr = " + oldAttr);
        console.log("readmore = " + readmore);
        console.log(oldAttr == readmore)
        if (oldAttr == readmore) {
            t = $(this).attr('read-less')
        } else {
            t = $(this).attr('read-more')
        };

        console.log("newAttr = " + t);
        return t
    });
    $(this).children('.read-more-toggle').toggleClass('read-more read-more-off', 200);
    $(element).tooltip('fixTitle').tooltip('show');
};

$(document).ready(function () {
    fixTooltip("");

    /*
     $('.accordion').collapse();

     $('.accordion').on('show hide', function(e){
     $(e.target).siblings('.accordion-heading').find('.accordion-toggle i').toggleClass('icon-arrow-down icon-arrow-up', 200);
     });
     */




    $('.expand').each(function () {
        var reducedHeight = $(this).height();
        $(this).css('height', 'auto');
        var fullHeight = $(this).height();
        $(this).height(reducedHeight);

        $(this).data('reducedHeight', reducedHeight);
        $(this).data('fullHeight', fullHeight);
    }).click(function () {
            readmore = $(this).attr('read-more')
            readless = $(this).attr('read-less')
            
            $(this).animate({height: $(this).height() == $(this).data('reducedHeight') ? $(this).data('fullHeight') : $(this).data('reducedHeight')}, 200);
            $(this).toggleClass('cursor-down cursor-up', 200);
            $(this).prop('title', function (idx, oldAttr) {
                console.log("oldAttr = " + oldAttr);
                console.log("readmore = " + readmore);
                console.log(oldAttr == readmore)
                if (oldAttr == readmore) {
                    t = $(this).attr('read-less')
                } else {
                    t = $(this).attr('read-more')
                }
                ;

                console.log("newAttr = " + t);
                return t
            });
            $(this).children('.read-more-toggle').toggleClass('read-more read-more-off', 200);
            $(element).tooltip('fixTitle').tooltip('show');
        });
});


//http://stackoverflow.com/questions/848797/yellow-fade-effect-with-jquery/13106698#13106698
//jQuery.fn.highlight = function () {
//    $(this).each(function () {
//        var el = $(this);
//        $("<div/>")
//            .width(el.outerWidth())
//            .height(el.outerHeight())
//            .css({
//                "position": "absolute",
//                "left": el.offset().left,
//                "top": el.offset().top,
//                "background-color": "#ffff99",
//                "opacity": ".7",
//                "z-index": "9999999"
//            }).appendTo('body').fadeOut(1000).queue(function () { $(this).remove(); });
//    });
//};

jQuery.fn.greyout = function () {
    $(this).each(function () {
        var el = $(this);
        $("<div class='greyout'/>")
            .width(el.outerWidth())
            .height(el.outerHeight())
            .css({
                "position": "absolute",
                "left": el.offset().left,
                "top": el.offset().top,
                "background-color": "#eeeeee",
                "opacity": ".7",
                "z-index": "9999999"
            }).appendTo(el)
    });
};

jQuery.fn.ungreyout = function () {
    $(this).each(function () {
        var el = $(this);
        el.find('.myclass').remove();
    });
};

jQuery.fn.highlight = function () {
    $(this).each(function () {
        $(this).css({backgroundColor: "#ffffcc"}).animate(
            {
                backgroundColor: "#ffffff"
            }, 1500) } );
};

$(document).on("eldarion-ajax:begin", function(evt, $el) {
    x = $($el.attr('spin'));
    x.spin();
    x.greyout();
    //x.css({backgroundColor: "#eeeeee"});
});

$(document).on("eldarion-ajax:complete", function(evt, $el) {
    x = $($el.attr('spin'));
    x.spin(false);
    x.ungreyout();
    //x.css({backgroundColor: "#ffffff"});
});
