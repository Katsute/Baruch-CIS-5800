# Baruch CIS 5800

CIS 5800 - Information Technology Development and Project Management

## Installation

#### 1️⃣ Request API Tokens

 1. Request a bus token at <https://bt.mta.info/wiki/Developers/Index>.
 2. Request a subway token at <https://api.mta.info/#/signup>.

#### 2️⃣ Prerequisites

 1. Download [Java 8](https://www.java.com/download).

#### 3️⃣ Download Required Resources

 1. Download the latest [release](https://github.com/Katsute/Baruch-CIS-5800/releases) and save it in a folder.

 2. Download the following static data feeds from <http://web.mta.info/developers/developer-data-terms.html#data>.

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
 3. Run the jar file (check the file name) to start the server.

    ```sh
    java -jar mta-information-site.jar
    ```
 4. Open your browser to `localhost:8080` see the live site.
 5. Add site pages to the `site` folder.

### 💼 License

This project is released under the [GNU General Public License (GPL) v2.0](https://github.com/KatsuteDev/Baruch-CIS-5800/blob/main/LICENSE).

### ⚠️ Disclaimer

 - [@Katsute](https://github.com/Katsute) is not affiliated with the MTA.
 - By using the MTA API you are subject to their [Terms and Conditions](https://api.mta.info/#/DataFeedAgreement).