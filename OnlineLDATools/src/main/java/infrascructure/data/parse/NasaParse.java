
package infrascructure.data.parse;


import infrascructure.data.PlainTextResource;
import infrascructure.data.Resource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by Roman on 08.02.14.
 */
public class NasaParse implements Parser {

    public static final String TEG_DIV_PANE_CONTENT = "div.pane-content";
    public static final String TEG_DIV_PANE_TITLE = "h2.pane-title";

    @Override
    public PlainTextResource parse(Resource r) {

        Document doc = Jsoup.parse(r.getData());

        //TODO:  element div.pane-content is not generic
        //doc.select("rss_description");

        Elements els = doc.body().getElementsByTag("p");
        StringBuilder sb = new StringBuilder("");
        for (Element el : els) {
            String text = el.text();
            sb.append(text).append("\n");
        }

        String tittle = doc.title();
        PlainTextResource resource = new PlainTextResource(sb.toString());
        resource.setTittle(tittle);
        return resource;

    }

    private PlainTextResource setPlainTextResource(Elements elementContent, Elements elementTitle) {
        PlainTextResource plainTextResource = null;
        for(Element element : elementContent)
        {
            plainTextResource= new PlainTextResource(element.text());
        }
        plainTextResource.setTittle( elementTitle.iterator().next().text());
        return plainTextResource;
    }

    private boolean validationEmptyElement(Elements elementContent, Elements elementTitle) {
        if(elementContent.isEmpty())
            return false;
        if(elementTitle.isEmpty())
            return false;
        return true;
    }
}
