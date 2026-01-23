package com.head2head.getabook.data.scripts.yandex

object YandexEarlyCss {
    const val SCRIPT = """
        (function() {
            const css = `
                .Root.Root_inited {
                    display: none !important;
                }
            `;
            const style = document.createElement("style");
            style.id = "getabook-root-hide-style";
            style.textContent = css;
            document.documentElement.appendChild(style);
        })();
    """
}
