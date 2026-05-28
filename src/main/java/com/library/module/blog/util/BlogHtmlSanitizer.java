package com.library.module.blog.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

@Component
public class BlogHtmlSanitizer {

    private static final String BASE_URI = "https://bookly.local";

    private final Safelist safelist = Safelist.relaxed()
            .addTags("figure", "figcaption")
            .addAttributes(":all", "class")
            .addAttributes("img", "src", "alt", "title", "width", "height", "loading")
            .addAttributes("a", "href", "title", "target")
            .addProtocols("a", "href", "http", "https", "mailto")
            .addProtocols("img", "src", "http", "https")
            .addEnforcedAttribute("a", "rel", "noopener noreferrer nofollow")
            .preserveRelativeLinks(true);

    private final Document.OutputSettings outputSettings = new Document.OutputSettings()
            .prettyPrint(false);

    public String sanitize(String rawHtml) {
        if (rawHtml == null) {
            return "";
        }

        return Jsoup.clean(rawHtml, BASE_URI, safelist, outputSettings);
    }

    public boolean isBlankContent(String rawHtml) {
        String cleanHtml = sanitize(rawHtml);
        String text = Jsoup.parse(cleanHtml).text();

        return text.isBlank() && !cleanHtml.contains("<img");
    }
}
