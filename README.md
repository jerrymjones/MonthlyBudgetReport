# MoneyDance Monthly Budget Report

For help using this extension see the [Wiki](https://github.com/jerrymjones/MonthlyBudgetReport/wiki).

Monthly Budget Report is an extension for the [Moneydance](https://moneydance.com/)
Personal Finance app to help you report how well you are meeting your monthly spending 
goals. The main reason to use this extension instead of the built in Moneydance 
budget report is that this extension properly handles negative budget amounts.
I see a budget as being fluid throughout the year. Budget amounts always need
to be updated throughout the year as spending changes and I like to be able to 
borrow from categories I'm under spending while allocating the extra to categories
that are becoming overspent. There may also be special occasions, such as when someone
refunds part of the money spent earlier in the year that you may need to enter a 
negative budget amount so that the budget accurately reflects the changes. An
example would be for car insurance. At times our insurer may issue a refund of
some of the earlier premium based on conditions during the year. Of course I 
could just adjust the earlier amounts to get to the proper amount spent but I'd
rather be able to track exactly what happened in the budget. If you don't want 
that ability, you may just want to use the default Moneydance budget report.

Monthly Budget Report is a companion to to my [Monthly Budget Editor](https://github.com/jerrymjones/MonthlyBudgetEditor) that not only
allows for negative budget amounts but also allows for easy entry of budget
amounts in a compact spreadsheet style format.

## Installation

1. Either [build](#build) the source code or [download](https://github.com/jerrymjones/MonthlyBudgetReport/releases/latest) the latest release.

2. Follow [Moneydance's official documentation to install extensions](https://help.infinitekind.com/support/solutions/articles/80000682003-installing-extensions).  
   Use the `Add From File...` option to load the `budgetreport.mxt` file.

3. **The extension has not yet been audited and signed by The Infinite Kind**, so you'll get a warning asking you if you really want to continue loading 
   the extension, click **Yes** to continue loading the extension.
   
4. You can now open the extension by going to **Extensions > Monthly Budget Report**.

## Build

1. Clone the repository to your local system:

```shell
git clone https://github.com/jerrymjones/MonthlyBudgetReport.git <localfolder>
```

2. Initialize the folder structure for building. The following command needs to be executed in `src/` i.e. `cd <localfolder>/src`:

```shell
ant init
```

3. Download the Moneydance [Developer's Kit](https://infinitekind.com/dev/moneydance-devkit-5.1.tar.gz) and extract it
   to a local folder on your system. Once extracted, copy-paste `lib/extadmin.jar` and `lib/moneydance-dev.jar` into the `<localfolder>/lib` folder:

```shell
cd tmp/
curl -O https://infinitekind.com/dev/moneydance-devkit-5.1.tar.gz
tar xzvf moneydance-devkit-5.1.tar.gz
cp moneydance-devkit-5.1/lib/* ... 
```

4. Generate a key pair (as required by Moneydance) to sign your locally built extension. You will be prompted for a passphrase that is used to
   encrypt the private key file. Your new keys will be stored in the priv_key and pub_key files. The command needs to be executed in `<localfolder>/src`:

```shell
ant genkeys
```

5. Build the extension from `<localfolder>/src`:

```shell
ant budgetreport
```

6. Install the extension per the installation instructions [above](#installation) using `<localfolder>/dist/budgetreport.mxt` as the file to load.
