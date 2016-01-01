package edu.cpp.iipl.crawlers.amazon.core;

import edu.cpp.iipl.crawlers.amazon.model.Product;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xing on 12/22/15.
 */
public class ProductCrawler extends Crawler {

    // get the url pointing to the product page
    private String getPageUrl(String asin) {
        return "http://www.amazon.com/dp/" + asin;
    }

    // parse the product page
    private Product parsePage(Document page) {
        if (page == null) return null;

        Product product = new Product();

        // name
        Element title = page.getElementsByTag("title").first();
        if (title != null)
            product.setName(title.ownText().trim());

        // model number
        // bot gets different page source (table) than browser (list) for this part
        // an alternative solution would be setting the user-agent in the request header
        // not sure i will do that. just keep it this way for now.
        Element model = page.select("tr > th:containsOwn(Item model number)").first();
        model = model.nextElementSibling();
        if (model != null && model.parent() != null) {
            String modelNum = model.ownText().trim();
            modelNum = modelNum.toUpperCase();

            product.setModelNum(modelNum);
        }

        // number of reviews
        product.setNumOfReviewsOnPage(0);    // init as 0
        Element numOfReviews = page.getElementById("acrCustomerReviewText");
        if (numOfReviews != null) {
            Pattern pattern = Pattern.compile("([,\\d]+) customer review");
            Matcher matcher = pattern.matcher(numOfReviews.ownText().trim());

            if (matcher.find()) {
                String pureNumber = matcher.group(1).replaceAll(",", "");
                product.setNumOfReviewsOnPage(Integer.parseInt(pureNumber));
            }
        }

        // update date (crawling date)
        product.setUpdateDate(new Date());

        // get image urls
        Elements scripts = page.getElementsByTag("script");
        if (scripts != null) {
            // note that image url might be "null"
            Pattern hiResPttn = Pattern.compile("'colorImages':.+?'initial':.+?\"hiRes\".+?(null|\"http.+?\")");
            Pattern largePttn = Pattern.compile("'colorImages':.+?'initial':.+?\"large\".+?(null|\"http.+?\")");
            for (Element script : scripts) {
                Matcher hiResMth = hiResPttn.matcher(script.html());
                if (hiResMth.find() && !hiResMth.group(1).equals("null"))
                    product.setImgUrlHiRes(hiResMth.group(1).replaceAll("\"", ""));

                Matcher largeMth = largePttn.matcher(script.html());
                if (largeMth.find() && !largeMth.group(1).equals("null"))
                    product.setImgUrlLarge(largeMth.group(1).replaceAll("\"", ""));

                // break out once obtained
                if (product.getImgUrlHiRes() != null || product.getImgUrlLarge() != null)
                    break;
            }
        }


        return product;
    }

    /**
     * Crawl a specific product info using its unique ASIN
     * @param asin              Amazon ASIN of the product
     * @return                  Key: ASIN, Value: product object
     */
    public Map<String, Product> crawlProduct(String asin) {
        Map<String, Product> productMap = new HashMap<>();

        // get page url
        String url = getPageUrl(asin);

        // get page
        Document page = getPage(url);

        // parse page and construct Product object
        Product product = parsePage(page);


        if (product != null) {
            // update product asin
            product.setAsin(asin);

            // update product page url
            product.setPageUrl(url);

            productMap.put(asin, product);

            logger.info("Product " + asin + " obtained.");
        }

        return productMap;
    }


    public static void main(String[] args) {
        ProductCrawler pc = new ProductCrawler();

        Map<String, Product> result = pc.crawlProduct("B00JMLCMKY");
    }
}
