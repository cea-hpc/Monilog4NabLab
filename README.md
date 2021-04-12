# Monilog4NabLab

[MoniLog](https://github.com/gemoc/monilog) integration for [NabLab](https://github.com/cea-hpc/NabLab).

This repository contains the necessary plugins to export an Eclipse deployable feature containing the Truffle-based NabLab interpreter (running on GraalVM), the MoniLog instrument for Truffle-based languages, and the UI plugin integrating those in the NabLab IDE.

## How to Build

Clone this repository and run the `build.sh` script by specifying the path to the NabLab sources as a first argument, and the path to the MoniLog sources as a second argument.

Then, import the projects in the plugins and features folders in your Eclipse workspace.

Go to **File** > **Export** > **Deployable features** and select `fr.cea.nabla.ui.graalvm.feature`.

In the **Destination** tab, select **Archive file** and select the path where you wish the archive to be created.

In the **Options** tab, select **Package as individual JAR archives**, **Generate p2 repository**, provide the `features/fr.cea.nabla.ui.graalvm.feature/category.xml` file for **Categorize repository**, and select **Allow for binary cycles in target platform**.

Then, click on **Finish**.


## How to Install

You can now install the generated archive through the **Help** > **Install New Software...** menu.

Click on the **Add..** button, then the **Archive...** button, and select your freshly exported archive.

The process is then exactly the same as when updating your Eclipse IDE.

During the installation, GraalVM Community Edition 21.0.0 will be downloaded in the installation folder of your IDE if not found there, and the Truffle-based NabLab interpreter and the MoniLog instrument will be deployed on it.

Once you restart your IDE, you will be able to define MoniLog files, and run Truffle-based interterpreted NabLab executions.


## Running a Truffle-Based Interpreted NabLab Execution

In the NabLab IDE, go to **Run** > **Run Configurations...** and create a new **NabLab Truffle-Based Interpreter Launch** configuration.

You then need to provide the NabLab project where your application is located, the path to the ngen file you wish to execute, and the corresponding json file specifying the application's options.

Alternatively, you can simply right-click on the ngen file and select **Run As** > **NabLab Truffle-Based Interpreter Launch** to create and run a default configuration.

These only allow to launch un-instrumented execution which will not produce any output.

If you wish to instrument the execution, for example to log the values of certain variables in your application, you can do so with MoniLog files (see [here](https://github.com/gemoc/monilog) for more details on how to specify MoniLog files).

To instrument an execution with a MoniLog file, indicate its path in the launch configuration.

If Python code is used in these MoniLog files, and you need it to run in a particular venv, you also need to specify the path to the Python executable to use in this venv.

