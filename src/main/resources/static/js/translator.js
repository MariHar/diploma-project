async function translateElements(elements, targetLang) {
    if (elements.length === 0 || !targetLang || targetLang === 'hy') return;

    for (const el of elements) {
        const originalText = el.innerText.trim();

        // Skip if empty or already translated
        if (!originalText || el.hasAttribute('data-translated')) continue;

        // Visual feedback
        el.setAttribute('data-translated', 'processing');
        el.style.transition = 'opacity 0.3s';
        el.style.opacity = '0.3';

        // Google Translate API (hy to target)
        const url = `https://translate.googleapis.com/translate_a/single?client=gtx&sl=hy&tl=${targetLang}&dt=t&q=${encodeURIComponent(originalText)}`;

        try {
            const response = await fetch(url);
            if (!response.ok) throw new Error("API Error");

            const data = await response.json();
            const translatedText = data[0].map(chunk => chunk[0]).join('');

            el.innerText = translatedText;
            el.setAttribute('data-translated', 'true');
        } catch (error) {
            console.error("Translation error:", error);
            el.removeAttribute('data-translated');
        } finally {
            el.style.opacity = '1';
        }
    }
}

// Watch for dynamic filtering changes
function observeDynamicInjections(targetLang) {
    const observer = new MutationObserver((mutations) => {
        mutations.forEach(mutation => {
            mutation.addedNodes.forEach(node => {
                if (node.nodeType === 1) {
                    const targets = node.classList.contains('translate-me') ? [node] : node.querySelectorAll('.translate-me');
                    if (targets.length > 0) translateElements(targets, targetLang);
                }
            });
        });
    });
    observer.observe(document.body, { childList: true, subtree: true });
}

document.addEventListener('DOMContentLoaded', () => {
    // 1. Get lang from URL (?lang=en)
    const urlParams = new URLSearchParams(window.location.search);
    const appLang = urlParams.get('lang');

    // 2. Only translate if language is NOT Armenian
    if (appLang && appLang !== 'hy') {
        const initialElements = document.querySelectorAll('.translate-me');
        translateElements(initialElements, appLang);
        observeDynamicInjections(appLang);
    }
});