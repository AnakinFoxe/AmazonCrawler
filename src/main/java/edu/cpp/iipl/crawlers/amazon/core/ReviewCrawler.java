package edu.cpp.iipl.crawlers.amazon.core;

import edu.cpp.iipl.crawlers.amazon.model.Product;
import edu.cpp.iipl.crawlers.amazon.model.Review;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xing on 12/22/15.
 */
public class ReviewCrawler extends Crawler {

    // formatter for string -> date conversion
    private DateFormat fmt_ = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);


    // get the url pointing to a page of reviews
    private String getPageUrl(String asin, int pageId) {
        return "http://www.amazon.com/product-reviews/"
                + asin
                + "/ref=cm_cr_pr_btm_link_"
                + pageId
                + "?ie=UTF8&pageNumber="
                + pageId;
    }


    // parse the page of reviews
    private Map<String, Review> parsePage(Document page) {
        if (page == null) return null;

        Map<String, Review> reviews = new HashMap<>();

        // get reviews
        Elements reviewElements = page.select("div.a-section.review");

        // parse each review
        for (Element reviewElement : reviewElements) {
            Review review = new Review();

            // name
            review.setName(reviewElement.id());

            // permalink
            review.setPermalink("http://www.amazon.com/review/" + reviewElement.id());

            // rating
            Element rate = reviewElement.select("span.a-icon-alt").first();
            if (rate != null) {
                Pattern pattern = Pattern.compile("([\\d]).0 out of 5 stars");
                Matcher matcher = pattern.matcher(rate.ownText());
                if (matcher.find())
                    review.setRate(Integer.parseInt(matcher.group(1)));
            }

            // title
            Element title = reviewElement.select("a.review-title").first();
            if (title != null)
                review.setTitle(title.ownText().trim());

            // date
            Element date = reviewElement.select("span.review-date").first();
            if (date != null) {
                try {
                    Pattern pattern = Pattern.compile("on (.+)");
                    Matcher matcher = pattern.matcher(date.ownText().trim());

                    if (matcher.find())
                        review.setDate(fmt_.parse(matcher.group(1)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            // help ratio
            Element helpRatio = reviewElement.select("span.review-votes").first();
            if (helpRatio != null) {
                Pattern pattern = Pattern.compile("([\\d]+) of ([\\d]+) people found the following review helpful");
                Matcher matcher = pattern.matcher(helpRatio.ownText());
                if (matcher.find()) {
                    float numerator = Float.parseFloat(matcher.group(1));
                    float denominator = Float.parseFloat(matcher.group(2));
                    review.setHelpRatio(numerator / denominator);
                }
            }

            // review text
            Element text = reviewElement.select("span.review-text").first();
            if (text != null)
                review.setText(text.ownText());

            reviews.put(review.getName(), review);
        }

        return reviews;
    }


    /**
     * Scrape a review page and obtain all the reviews
     * @param asin                  Amazon ASIN of the product
     * @param nPage                 The specific page of the product review
     * @return                      Key: review id, Value: review object
     */
    public Map<String, Review> scrapePage(String asin, int nPage) {
        // get page url
        String url = getPageUrl(asin, nPage);

        // get page
        Document page = getPage(url);

        // parse page and construct Review objects
        Map<String, Review> reviews = parsePage(page);

        logger.info("[" + asin + "] Done parsing review page " + nPage + "...");

        return reviews;
    }


    /**
     * Crawl all the reviews of the specified product using product ASIN
     * @param asin                  Amazon ASIN of the product
     * @return                      Key: review id, Value: review object
     */
    public Map<String, Review> crawlReviews(String asin) {
        Map<String, Review> reviews = new HashMap<>();

        int nPage = 1;
        while (true) {
            // parse page and construct Review objects
            Map<String, Review> pageReviews = scrapePage(asin, nPage++);

            // merge into one Map
            if (pageReviews != null && !pageReviews.isEmpty())
                reviews.putAll(pageReviews);
            else {
                logger.info("[" + asin + "] Done Parsing Reviews at Page " + nPage);
                break;
            }
        }

        return reviews;
    }

    class ReviewCrawlerThread implements Callable<Map<String, Review>> {
        private String asin;
        private int nPage;
        private boolean enableVerbose;

        private ReviewCrawlerThread() {}

        public ReviewCrawlerThread(String asin, int nPage, boolean enableVerbose) {
            super();
            this.asin = asin;
            this.nPage = nPage;
            this.enableVerbose = enableVerbose;
        }

        @Override
        public Map<String, Review> call() throws Exception {
            ReviewCrawler rc = new ReviewCrawler();
            if (enableVerbose)
                rc.enableVerbose();

            return rc.scrapePage(asin, nPage);
        }
    }

    /**
     * Crawl all the reviews of the specified product using product object.
     * @param product           Already obtained product object
     * @return                  Key: review id, Value: review object
     */
    public Map<String, Review> crawlReviewsMT(Product product) {
        Map<String, Review> reviews = new HashMap<>();

        if (product != null) {
            int numOfReviews = product.getNumOfReviewsOnPage();

            // create thread pool according to number of pages to be crawled
            if (numOfReviews > 0) {
                int numOfPages = numOfReviews / 10 + 1;     // Amazon has 10 reviews per page
                int numOfThreads = Math.min(numOfPages, 8); // let's use at most 8 threads

                // create thread pool and send out crawler thread
                ExecutorService es = Executors.newFixedThreadPool(numOfThreads);
                List<Future<Map<String, Review>>> futureList = new ArrayList<>();
                for (int nPage = 1; nPage <= numOfPages; ++nPage)
                    futureList.add(es.submit(new ReviewCrawlerThread(product.getAsin(),
                            nPage, logger.isVerbose())));

                // shutdown executors once tasks are done, or expired
                es.shutdown();
                try {
                    es.awaitTermination(2, TimeUnit.HOURS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // merge all the results
                for (Future<Map<String, Review>> future : futureList) {
                    try {
                        reviews.putAll(future.get());
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return reviews;
    }


    /**
     * Crawl all the reviews of the specified product using product ASIN.
     * Multi-threading version.
     * @param asin              Amazon ASIN of the product
     * @return                  Key: review id, Value: review object
     */
    public Map<String, Review> crawlReviewsMT(String asin) {
        Map<String, Review> reviews = new HashMap<>();

        // get the product info to obtain total number of reviews
        ProductCrawler pc = new ProductCrawler();
        Map<String, Product> productMap = pc.crawlProduct(asin);

        if (!productMap.isEmpty()) {
            Product product = productMap.get(asin);

            reviews = crawlReviewsMT(product);
        }

        return reviews;
    }

    public static void main(String[] args) {
        ReviewCrawler rc = new ReviewCrawler();

        rc.crawlReviewsMT("B0083FTVB8");
    }



}
