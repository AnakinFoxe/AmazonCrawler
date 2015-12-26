package edu.cpp.iipl.crawlers.amazon.core;

import edu.cpp.iipl.crawlers.amazon.util.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Created by xing on 12/22/15.
 */
public class Crawler {

    // maximum retry times
    private int maxRetries_ = 10;

    // increment amount for each retry
    private int increment_ = 5000;

    // logger
    protected Logger logger;

    public Crawler() {
        this.logger = new Logger();
    }

    public int getMaxRetries_() {
        return maxRetries_;
    }

    public void setMaxRetries_(int maxRetries_) {
        this.maxRetries_ = maxRetries_;
    }

    public int getIncrement_() {
        return increment_;
    }

    public void setIncrement_(int increment_) {
        this.increment_ = increment_;
    }

    public void enableVerbose() { this.logger.setVerbose(true); }

    public void disableVerbose() { this.logger.setVerbose(false); }


    /**
     * Get the page in Jsoup Document
     * @param url           URL of the webpage
     * @return              Jsoup Document
     */
    protected Document getPage(String url) {
        // get the HTML page
        for (int retry = 1; retry <= maxRetries_; ++retry) {
            try {
                Document page = Jsoup.connect(url).get();

                return page;
            } catch (IOException e) {
                logger.warn("IOException (" + url + "). Retrying "
                        + retry + "/" + maxRetries_
                        + ". " + e.getMessage());

                // incremental waiting
                try {
                    Thread.sleep(3000 + increment_ * retry);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }

        return null;
    }

}
