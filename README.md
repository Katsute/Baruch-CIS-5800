# Baruch CIS 5800

**CIS 5800 - Information Technology Development and Project Management**

Website that allows users to view upcoming stops and alerts for their current trip, displaying information visually or translated.

## Installation

#### 1️⃣ Request API Tokens

 1. Request a bus token at <https://bt.mta.info/wiki/Developers/Index>.
 2. Request a subway token at <https://api.mta.info/#/signup>.

#### 2️⃣ Prerequisites

 1. Download [Java 8](https://www.java.com/download).

#### 3️⃣ Download Required Resources

 1. Download `all.zip` from the latest [release](https://github.com/Katsute/Baruch-CIS-5800/releases).
 2. Extract the zip contents into a folder.

#### 4️⃣ Setup the Program

 1. Write the bus token to a file named `bus-token.txt` in the same directory.
 2. Write the subway token to a file named `subway-token.txt` in the same directory.

    Your folder should look like this:

    ![setup](https://raw.githubusercontent.com/Katsute/Baruch-CIS-5800/main/setup.png)

 3. Run the jar file from either:

    * The command line using `java -jar mta-information-site.jar`
    * The batch file `run.bat` (Windows)
    * The shell file `run.sh` (Bash)

    **Do not click the jar file itself,** this will run the application in the background and will require using the task manager to close it.

 4. Open your browser to `localhost:8080` see the live site.

### 💼 License

This project is released under the [GNU General Public License (GPL) v2.0](https://github.com/Katsute/Baruch-CIS-5800/blob/main/LICENSE).

This project is for educational purposes only.

Names, logos, subway symbols are owned by the MTA "Metropolitan Transit Authority" and are covered under "fair use".

Copyright Disclaimer under section 107 of the Copyright Act of 1976; Allowance is made for "Fair Use" for purposes such as criticism, comment, news reporting, teaching, scholarship, and research.

Fair use is a use permitted by copyright statute that might otherwise be infringing. Non-profit, educational or personal use tips the balance in favor of fair use.