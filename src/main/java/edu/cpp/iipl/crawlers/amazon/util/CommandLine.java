package edu.cpp.iipl.crawlers.amazon.util;

import edu.cpp.iipl.crawlers.amazon.core.ProductCrawler;
import edu.cpp.iipl.crawlers.amazon.core.ReviewCrawler;
import edu.cpp.iipl.crawlers.amazon.model.Product;
import edu.cpp.iipl.crawlers.amazon.model.Review;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by xing on 12/24/15.
 */
public class CommandLine {

    // display help information
    private static void displayHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("This program sends Amazon Crawler which obtains specified product and its reviews.\n");
        sb.append("Usage: AmazonCrawler [options] <ASIN/file> <dir>\n");
        sb.append("Options:\n");
        sb.append("  -h        Display help information\n");
        sb.append("  -m        Utilize multi-threading\n");
        sb.append("  -v        Produce verbose output. Normally for debugging.\n");
        sb.append("Input:\n");
        sb.append("  <ASIN>    Amazon ASIN of the product\n");
        sb.append("  <file>    File containing list of ASINs in \"ASIN::product name\" format\n");
        sb.append("  <dir>     Directory for crawled results\n");
        sb.append("Example: AmazonCrawler -mv B0083FTVB8 A_Random_Folder\n");
        System.out.println(sb.toString());
    }

    // check valid ASIN
    private static boolean validAsin(String asin) {
        return asin != null
                && asin.length() == 10                                  // ASIN length is always 10 characters
                && asin.charAt(0) != '-'                                // first char can not be '-' (non-options)
                && asin.replaceAll("[0-9a-zA-Z]", "").length() == 0;    // ASIN contains only digits and number
    }

    // a very basic input check
    private static boolean inputCheck(String asin, String dir) {
        return  validAsin(asin) && dir != null && dir.charAt(0) != '-';
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

    // crawl a single product
    private static void crawlSingleProduct(String asin, String dir, boolean enableVerbose, boolean enableMT)
            throws IOException {
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
        System.out.println("Cost of time: " + ((endCrawl - startCrawl) / 1000) + "s for crawling, "
                + (endWrite - startWrite) + "ms for writing");
    }

    private static void crawlBatchProducts(String filePath, String dir, boolean enableMT)
            throws IOException {
        if (!new File(filePath).exists()) {
            System.out.println("The input file (" + filePath + ") does not exist. Please check it.");
            return;
        }

        // prepare the task list (products to be crawled)
        List<String[]> taskList = new ArrayList<>();
        FileReader fr = new FileReader(filePath);
        try (BufferedReader br = new BufferedReader(fr)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] task = line.trim().split("::");
                if (validAsin(task[0]))
                    taskList.add(task);
            }
        }
        fr.close();
        System.out.println("Total " + taskList.size() + " products to be crawled");

        // prepare crawler
        ProductCrawler pc = new ProductCrawler();
        ReviewCrawler rc = new ReviewCrawler();

        // disable log for batch mode
        pc.disableVerbose();
        rc.disableVerbose();
        System.out.println("Log disabled in batch mode");


        // prepare folder and file
        File dst = new File(dir);
        if (!dst.exists())
            dst.mkdir();
        String savePath = dst.getCanonicalPath() + "/reviews.txt";
        FileWriter fw = new FileWriter(savePath, true);
        BufferedWriter bw = new BufferedWriter(fw);


        long start = System.currentTimeMillis();
        for (int i = 0; i < taskList.size(); ++i) {
            String asin = taskList.get(i)[0];
            String productName = taskList.get(i)[1];

            System.out.print("Crawling " + (i + 1) + " of " + taskList.size() +
                                " product: [" + asin + "] " + productName + "...");

            // crawl product and reviews
            long startCrawl = System.currentTimeMillis();
            Map<String, Product> productMap = pc.crawlProduct(asin);
            Map<String, Review> reviewMap;
            if (enableMT)
                reviewMap = rc.crawlReviewsMT(productMap.get(asin));
            else
                reviewMap = rc.crawlReviews(asin);

            // write to file
            for (String key : reviewMap.keySet()) {
                bw.write(reviewMap.get(key).getText() + "\n");
            }
            long endCrawl = System.currentTimeMillis();

            System.out.println(" Done (" + ((endCrawl - startCrawl) / 1000) + "s)");
        }
        long end = System.currentTimeMillis();

        System.out.println("Done crawling. Total cost of time: " + ((end - start) / 1000) + "s");
        bw.close();
        fw.close();
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
        boolean batchProcess = false;

        String param1 = null;
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
                        case 'b':
                            batchProcess = true;
                            break;
                        case 'h':
                        default:
                            displayHelp();
                            return;
                    }
                }
            } else {
                param1 = args[i++];
                if (i < args.length)
                    dir = args[i];
                else
                    displayHelp();
            }
        }

        if (batchProcess) {
            // in batch process, param1 is a file
            crawlBatchProducts(param1, dir, enableMT);
        } else
            // in single process, param1 is asin
            crawlSingleProduct(param1, dir, enableVerbose, enableMT);

    }
}
