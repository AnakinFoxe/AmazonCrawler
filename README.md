# AmazonCrawler

This program sends Amazon Crawler which obtains specified product and its reviews.

Usage: `AmazonCrawler [options] <ASIN> <dir>`

Options:

    -h        Display help information
    -m        Utilize multi-threading
    -v        Produce verbose output. Normally for debugging.

Input:
  
    <ASIN>    Amazon ASIN of the product
    <dir>     Directory for crawled results

Example: `AmazonCrawler -mv B00VSIT5UE A_Random_Folder`
