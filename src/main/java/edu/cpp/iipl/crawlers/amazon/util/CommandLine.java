package edu.cpp.iipl.crawlers.amazon.util;

import edu.cpp.iipl.crawlers.amazon.core.ProductCrawler;
import edu.cpp.iipl.crawlers.amazon.core.ReviewCrawler;
import edu.cpp.iipl.crawlers.amazon.model.Product;
import edu.cpp.iipl.crawlers.amazon.model.Review;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Created by xing on 12/24/15.
 */
public class CommandLine {

    // display help information
    private static void displayHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("This program sends Amazon Crawler which obtains specified product and its reviews.\n");
        sb.append("Usage: AmazonCrawler [options] <ASIN> <dir>\n");
        sb.append("Options:\n");
        sb.append("  -h        Display help information\n");
        sb.append("  -m        Utilize multi-threading\n");
        sb.append("  -v        Produce verbose output. Normally for debugging.\n");
        sb.append("Input:\n");
        sb.append("  <ASIN>    Amazon ASIN of the product\n");
        sb.append("  <dir>     Directory for crawled results\n");
        sb.append("Example: AmazonCrawler -mv B0083FTVB8 A_Random_Folder\n");
        System.out.println(sb.toString());
    }

    // a very basic input check
    private static boolean inputCheck(String asin, String dir) {
        return  asin != null && dir != null
                && asin.charAt(0) != '-' && dir.charAt(0) != '-'        // first char can not be '-' (non-options)
                && asin.length() == 10                                  // ASIN length is always 10 characters
                && asin.replaceAll("[0-9a-zA-Z]", "").length() == 0;    // ASIN contains only digits and number
    }

    // format product info
    private static String formatProduct(Product product) {
        StringBuilder sb = new StringBuilder();

        sb.append("Name: ");
        sb.append(product.getName());
        sb.append("\nASIN: ");
        sb.append(product.getAsin());
        sb.append("\nModel Number: ");
        sb.append(product.getModelNum());
        sb.append("\nNumber of Reviews: ");
        sb.append(product.getNumOfReviewsOnPage());
        sb.append("\nCrawled Date: ");
        sb.append(product.getUpdateDate());
        sb.append("\nHigh Resolution Image URL: ");
        sb.append(product.getImgUrlHiRes());
        sb.append("\nMain Image URL: ");
        sb.append(product.getImgUrlLarge());

        return sb.toString();
    }

    // format review info
    private static String formatReview(Review review) {
        StringBuilder sb = new StringBuilder();

        sb.append("Name: ");
        sb.append(review.getName());
        sb.append("\nTitle: ");
        sb.append(review.getTitle());
        sb.append("\nDate: ");
        sb.append(review.getDate());
        sb.append("\nRating: ");
        sb.append(review.getRate());
        sb.append("\nHelpful Ratio: ");
        sb.append(review.getHelpRatio());
        sb.append("\nPermalink: ");
        sb.append(review.getPermalink());
        sb.append("\nText: ");
        sb.append(review.getText());

        return sb.toString();
    }


    /**
     * Taking command line input to initiate Amazon Crawler.
     *
     * Options:
     *  -h                  Display help information
     *  -m                  Utilize multi-threading
     *  -v                  Produce verbose output. Normally for debugging.
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            displayHelp();
            return;
        }

        boolean enableVerbose = false;
        boolean enableMT = false;

        String asin = null;
        String dir = null;

        // parse input and options
        for (int i = 0; i < args.length; ++i) {
            if (args[i].charAt(0) == '-') {
                for (int j = 1; j < args[i].length(); ++j) {
                    switch (args[i].charAt(j)) {
                        case 'm':
                            enableMT = true;
                            break;
                        case 'v':
                            enableVerbose = true;
                            break;
                        case 'h':
                        default:
                            displayHelp();
                            return;
                    }
                }
            } else {
                asin = args[i++];
                if (i < args.length)
                    dir = args[i];
                else
                    displayHelp();
            }
        }

        // check input
        if (!inputCheck(asin, dir)) {
            System.out.println("The input ASIN (" + asin + ") or dir (" + dir + ") is invalid. Please check it.");
            return;
        }

        // prepare and send crawlers
        long startCrawl = System.currentTimeMillis();
        ProductCrawler pc = new ProductCrawler();
        ReviewCrawler rc = new ReviewCrawler();
        if (enableVerbose) {
            pc.enableVerbose();
            rc.enableVerbose();
        }
        Map<String, Product> productMap = pc.crawlProduct(asin);
        Map<String, Review> reviewMap;
        if (enableMT)
            reviewMap = rc.crawlReviewsMT(productMap.get(asin));
        else
            reviewMap = rc.crawlReviews(asin);
        long endCrawl = System.currentTimeMillis();

        // process and store data
        // 1. create destination folder
        long startWrite = System.currentTimeMillis();
        File dst = new File(dir);
        if (!dst.exists())
            dst.mkdir();
        // 2. create base folder (product folder)
        String basePath = dst.getCanonicalPath() + "/" + asin;
        File base = new File(basePath);
        if (!base.exists())
            base.mkdir();
        // 3. create product file
        if (!productMap.isEmpty()) {
            FileWriter fw = new FileWriter(basePath + "/product.txt", false);
            try (BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(formatProduct(productMap.get(asin)));
            }
            fw.close();
        }
        // 4. create review files
        if (!reviewMap.isEmpty()) {
            for (Map.Entry<String, Review> review : reviewMap.entrySet()) {
                FileWriter fw = new FileWriter(basePath + "/review."
                        + review.getKey() + ".txt", false);
                try (BufferedWriter bw = new BufferedWriter(fw)) {
                    bw.write(formatReview(review.getValue()));
                }
                fw.close();
            }
        }
        long endWrite = System.currentTimeMillis();


        System.out.println("Product and Review information obtained for " + asin);
        System.out.println("Cost of time: " + (endCrawl - startCrawl) + "ms for crawling, "
                                            + (endWrite - startWrite) + "ms for writing");
    }
}
