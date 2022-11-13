# Baruch CIS 5800

CIS 5800 - Information Technology Development and Project Management

## Installation

#### 1️⃣ Request API Tokens

 1. Request a bus token at <https://bt.mta.info/wiki/Developers/Index>.
 2. Request a subway token at <https://api.mta.info/#/signup>.

#### 2️⃣ Prerequisites

 1. Download [Java 8](https://www.java.com/download).

#### 3️⃣ Download Required Resources

 1. Download `site.zip` and `mta-information-site.jar` from the latest [release](https://github.com/Katsute/Baruch-CIS-5800/releases).

 2. ```
    ⚠️ If you are using v2 or higher you can skip the below step.

    This program now automatically downloads static data from the MTA.
    ```

    Download the following static data feeds from <http://web.mta.info/developers/developer-data-terms.html#data>.

    - **New York City Transit Subway**
    - New York City Transit Bus
      - **Bronx**
      - **Brooklyn**
      - **Manhattan**
      - **Queens**
      - **Staten Island**
    - **Bus Company**

 3. Save these zip files in the same folder as this program. The zip files **must** be named as the following:

    - `google_transit_subway.zip`
    - `google_transit_bronx.zip`
    - `google_transit_brooklyn.zip`
    - `google_transit_manhattan.zip`
    - `google_transit_queens.zip`
    - `google_transit_staten_island.zip`
    - `google_transit_bus_company.zip`

#### 4️⃣ Setup the Program

 1. Write the bus token to a file named `bus-token.txt` in the same directory.
 2. Write the subway token to a file named `subway-token.txt` in the same directory.
 3. Run the jar file from the command line to start the server. **Do not click the jar**, if you click the jar you will have to use task manager to close it.

    ```sh
    java -jar mta-information-site.jar
    ```
 4. Open your browser to `localhost:8080` see the live site.
 5. Modify site pages in the `site` folder.

Your folder should look like this:

![setup](https://raw.githubusercontent.com/Katsute/Baruch-CIS-5800/main/setup.png)

### 💼 License

This project is released under the [GNU General Public License (GPL) v2.0](https://github.com/Katsute/Baruch-CIS-5800/blob/main/LICENSE).

This project is for educational purposes only.

Names, logos, subway symbols are owned by the MTA "Metropolitan Transit Authority" and are covered under "fair use".

Copyright Disclaimer under section 107 of the Copyright Act of 1976; Allowance is made for "Fair Use" for purposes such as criticism, comment, news reporting, teaching, scholarship, and research.

Fair use is a use permitted by copyright statute that might otherwise be infringing. Non-profit, educational or personal use tips the balance in favor of fair use.