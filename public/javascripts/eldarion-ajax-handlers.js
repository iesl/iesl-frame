/* ====================================================================
 * eldarion-ajax-handlers.js v0.1.1
 * ====================================================================
 * Copyright (c) 2013, Eldarion, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 * 
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 * 
 *     * Neither the name of Eldarion, Inc. nor the names of its contributors may
 *       be used to endorse or promote products derived from this software without
 *       specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ==================================================================== */

/*jshint forin:true, noarg:true, noempty:true, eqeqeq:true, bitwise:true,
  strict:true, undef:true, unused:true, curly:true, browser:true, jquery:true,
  indent:4, maxerr:50 */

(function ($) {
    'use strict';

    var Handlers = function () {};

    Handlers.prototype.redirect = function(e, $el, data) {
        if (data.location) {
            window.location.href = data.location;
            return false;
        }
    };
    Handlers.prototype.evaluateBefore = function(e, $el, data) {
        // presumably the eval'd code can access (e, $el, data)
        eval(data.evaluateBefore);
    };
    Handlers.prototype.evaluateAfter = function(e, $el, data) {
        eval(data.evaluateAfter);
    };
    Handlers.prototype.replace = function(e, $el, data) {
        // this tortured construction is needed to capture each new DOM node in the case of multiple replacements.
        $($el.data('replace')).each(function( index ) {
            var $d = $(data.noselector.html);
            $(this).replaceWith($d);
            new Function(data.noselector.js).call($d);
        });
    };
    Handlers.prototype.error = function(e, $el, data) {
        // this tortured construction is needed to capture each new DOM node in the case of multiple replacements.
        $($el.data('error')).each(function( index ) {
            var $d = $(data.noselector.error);
            $(this).replaceWith($d);
            new Function(data.noselector.js).call($d);
        });
    };
    Handlers.prototype.replaceClosest = function(e, $el, data) {
        $el.closest($el.data('replace-closest')).each(function( index ) {
            var $d = $(data.noselector.html);
            $(this).replaceWith($d);
            new Function(data.noselector.js).call($d);
        });
    };
    Handlers.prototype.errorClosest = function(e, $el, data) {
        $el.closest($el.data('error-closest')).each(function( index ) {
            var $d = $(data.noselector.error);
            $(this).replaceWith($d);
            new Function(data.noselector.js).call($d);
        });
    };
    Handlers.prototype.replaceInner = function(e, $el, data) {
        $($el.data('replace-inner')).each(function( index ) {
            var $this = $(this);
            $this.html(data.noselector.html);
            new Function(data.noselector.js).call($this);
        });
    };
    Handlers.prototype.replaceClosestInner = function(e, $el, data) {
        $el.closest($el.data('replace-closest-inner')).each(function( index ) {
            var $this = $(this);
            $this.html(data.noselector.html);
            new Function(data.noselector.js).call($this);
        });
    };
    Handlers.prototype.append = function(e, $el, data) {
        $($el.data('append')).each(function( index ) {
            var $d = $(data.noselector.html);
            $(this).append($d);
            new Function(data.noselector.js).call($d);
        });
    };
    Handlers.prototype.prepend = function(e, $el, data) {
        $($el.data('prepend')).each(function( index ) {
            var $d = $(data.noselector.html);
            $(this).prepend($d);
            new Function(data.noselector.js).call($d);
        });
    };
    Handlers.prototype.refresh = function(e, $el) {
        $.each($($el.data('refresh')), function(index, value) {
            $.getJSON($(value).data('refresh-url'), function(data) {
                $(value).each(function( index ) {
                    var $d = $(data.noselector.html);
                    $(this).replaceWith($d);
                    new Function(data.noselector.js).call($d);
                });
            });
        });
    };
    Handlers.prototype.refreshClosest = function(e, $el) {
        $.each($($el.data('refresh-closest')), function(index, value) {
            $.getJSON($(value).data('refresh-url'), function(data) {
                $el.closest($(value)).each(function( index ) {
                    var $d = $(data.noselector.html);
                    $(this).replaceWith($d);
                    new Function(data.noselector.js).call($d);
                });
            });
        });
    };
    Handlers.prototype.clear = function(e, $el) {
        $($el.data('clear')).each(function( index ) {
            var $this = $(this);
            $this.html('');
        });
    };
    Handlers.prototype.remove = function(e, $el) {
        $($el.data('remove')).remove();
        // there is no longer a node on which to trigger eldarion-ajax:affected
        // (we assume the parent doesn't need it, just like append etc.)
        // OTOH, maybe the handler does stuff to the parent, so it does want to be called. ??
    };
    Handlers.prototype.clearClosest = function(e, $el) {
        $el.closest($el.data('clear-closest')).each(function( index ) {
            var $this = $(this);
            $this.html('');
        });
    };
    Handlers.prototype.removeClosest = function(e, $el) {
        $el.closest($el.data('remove-closest')).remove();
    };
    Handlers.prototype.fragments = function(e, $el, data) {
        if (data.fragments) {
            $.each(data.fragments, function (i, s) {
                $(i).each(function( index ) {
                    var $d = $(s.html);
                    $(this).replaceWith($d);
                    new Function(s.js).call($d);
                });
            });
        }
        if (data['inner-fragments']) {
            $.each(data['inner-fragments'], function(i, s) {
                $(i).each(function( index ) {
                    var $this = $(this);
                    $this.html(s.html);
                    new Function(s.js).call($this);
                });
            });
        }
        if (data['append-fragments']) {
            $.each(data['append-fragments'], function(i, s) {
                $(i).each(function( index ) {
                    var $d = $(s.html);
                    $(this).append($d);
                    new Function(s.js).call($d);
                });
            });
        }
        if (data['prepend-fragments']) {
            $.each(data['prepend-fragments'], function(i, s) {
                $(i).each(function( index ) {
                    var $d = $(s.html);
                    $(this).append($d);
                    new Function(s.js).call($d);
                });
            });
        }
    };

    $(function () {
        $(document).on('eldarion-ajax:success', Handlers.prototype.redirect);
        $(document).on('eldarion-ajax:success', Handlers.prototype.evaluateBefore);
        $(document).on('eldarion-ajax:success', Handlers.prototype.fragments);
        $(document).on('eldarion-ajax:success', '[data-error]', Handlers.prototype.error);
        $(document).on('eldarion-ajax:success', '[data-error-closest]', Handlers.prototype.errorClosest);
        $(document).on('eldarion-ajax:success', '[data-replace]', Handlers.prototype.replace);
        $(document).on('eldarion-ajax:success', '[data-replace-closest]', Handlers.prototype.replaceClosest);
        $(document).on('eldarion-ajax:success', '[data-replace-inner]', Handlers.prototype.replaceInner);
        $(document).on('eldarion-ajax:success', '[data-replace-closest-inner]', Handlers.prototype.replaceClosestInner);
        $(document).on('eldarion-ajax:success', '[data-append]', Handlers.prototype.append);
        $(document).on('eldarion-ajax:success', '[data-prepend]', Handlers.prototype.prepend);
        $(document).on('eldarion-ajax:success', '[data-refresh]', Handlers.prototype.refresh);
        $(document).on('eldarion-ajax:success', '[data-refresh-closest]', Handlers.prototype.refreshClosest);
        $(document).on('eldarion-ajax:success', '[data-clear]', Handlers.prototype.clear);
        $(document).on('eldarion-ajax:success', '[data-remove]', Handlers.prototype.remove);
        $(document).on('eldarion-ajax:success', '[data-clear-closest]', Handlers.prototype.clearClosest);
        $(document).on('eldarion-ajax:success', '[data-remove-closest]', Handlers.prototype.removeClosest);
        $(document).on('eldarion-ajax:success', Handlers.prototype.evaluateAfter);
    });
}(window.jQuery));
