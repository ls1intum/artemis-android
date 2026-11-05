/* Android WebView caches some SVG tspan metrics before ReactFlow applies its fit scale.
 * Invalidate SVG layout atomically after scale/font changes; retain the diagram's geometry. */
(function () {
    if (window.ArtemisNativeSvgRepair) return;
    var canvas = null, observer = null, frame = null, disposed = true, lastScale = null;
    function scale() {
        var viewport = canvas && canvas.querySelector('.react-flow__viewport');
        if (!viewport) return null;
        var transform = getComputedStyle(viewport).transform;
        var parts = transform.match(/^matrix\(([^)]+)\)$/);
        if (!parts) return transform;
        return parts[1].split(',').slice(0, 4).map(function (part) { return Number(part.trim()); }).join(',');
    }
    function schedule() {
        if (disposed || frame !== null) return;
        frame = requestAnimationFrame(function () {
            frame = null;
            if (disposed || !canvas) return;
            lastScale = scale();
            var svgs = Array.from(canvas.querySelectorAll('.react-flow__node svg'));
            var displays = svgs.map(function (svg) {
                return [svg.style.getPropertyValue('display'), svg.style.getPropertyPriority('display')];
            });
            try {
                svgs.forEach(function (svg) { svg.style.setProperty('display', 'none', 'important'); });
                // Flush and restore within the same frame: no hidden frame or authored style changes.
                if (svgs.length) void document.body.offsetHeight;
            } finally {
                svgs.forEach(function (svg, index) {
                    var previous = displays[index];
                    if (previous[0]) svg.style.setProperty('display', previous[0], previous[1]);
                    else svg.style.removeProperty('display');
                });
            }
        });
    }
    function dispose() {
        disposed = true;
        if (observer) observer.disconnect();
        if (frame !== null) cancelAnimationFrame(frame);
        if (document.fonts) document.fonts.removeEventListener('loadingdone', schedule);
        observer = null; frame = null; canvas = null; lastScale = null;
    }
    window.ArtemisNativeSvgRepair = {
        attach: function (target) {
            if (!target || (!disposed && canvas === target)) return;
            dispose(); canvas = target; disposed = false;
            observer = new MutationObserver(function (mutations) {
                var newContent = mutations.some(function (change) { return change.type === 'childList'; });
                var viewportChanged = mutations.some(function (change) {
                    return change.type === 'attributes' && change.target.classList && change.target.classList.contains('react-flow__viewport');
                });
                // Our display mutations target SVGs, never the viewport, and cannot feed this loop.
                if (newContent || (viewportChanged && scale() !== lastScale)) schedule();
            });
            observer.observe(canvas, {subtree: true, childList: true, attributes: true, attributeFilter: ['style']});
            if (document.fonts) {
                document.fonts.addEventListener('loadingdone', schedule);
                document.fonts.ready.then(function () { if (!disposed && canvas === target) schedule(); });
            }
            schedule();
        },
        dispose: dispose
    };
})();
