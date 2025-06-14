package net.microfalx.heimdall.llm.web.chat;

import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import net.microfalx.lang.StringUtils;

import java.io.StringWriter;

import static net.microfalx.lang.StringUtils.EMPTY_STRING;

/**
 * Renders Markdown content into HTML.
 */
public class MarkdownRenderer {

    /**
     * Renders the given Markdown content into HTML.
     *
     * @param markdown the Markdown content to render
     * @return the rendered HTML content
     */
    public String render(String markdown) {
        if (StringUtils.isEmpty(markdown)) return EMPTY_STRING;
        // initialize the HTML renderer
        MutableDataSet options = new MutableDataSet();
        updateOptions(options);
        // create the parser
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();
        // render the file
        Node document = parser.parse(markdown);
        StringWriter writer = new StringWriter();
        renderer.render(document, writer);
        return writer.toString();
    }

    private void updateOptions(MutableDataSet options) {
        // uncomment to convert soft-breaks to hard breaks
        //options.set(HtmlRenderer.SOFT_BREAK, HtmlRenderer.HARD_BREAK.getDefaultValue());
        options.set(TablesExtension.CLASS_NAME, "table table-striped");
        options.set(HtmlRenderer.GENERATE_HEADER_ID, true);
        options.set(AnchorLinkExtension.ANCHORLINKS_ANCHOR_CLASS, "anchor");
        options.set(AnchorLinkExtension.ANCHORLINKS_SET_NAME, true);
    }
}
