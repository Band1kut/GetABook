package com.head2head.getabook.data.scripts.yandex

object YandexRootHideJs {
    const val SCRIPT = """
        (function() {

            const css = `
                .Root.Root_inited {
                    display: none !important;
                }
            `;

            function applyCSS() {
                try {
                    if (!document.getElementById("getabook-root-hide-style")) {
                        const style = document.createElement("style");
                        style.id = "getabook-root-hide-style";
                        style.textContent = css;
                        document.head.appendChild(style);
                        console.log("GetABook: CSS injected");
                    }
                } catch (e) {
                    console.log("GetABook: error injecting CSS", e);
                }
            }

            applyCSS();

            try {
                const observer = new MutationObserver(() => {
                    applyCSS();
                });
                observer.observe(document.documentElement, { childList: true, subtree: true });
            } catch (e) {
                console.log("GetABook: observer error", e);
            }
        })();
    """
}
