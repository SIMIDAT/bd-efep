# Big Data algorithm for the Extraction of Fuzzy Emerging Patterns (BD-EFEP)

In this repository the source code of the BD-EFEP algorithm is presented. Please cite this work as:

*García-Vico, Á. M., González, P., Carmona, C. J., & del Jesus, M. J. (In Press). A Big Data Approach for the Extraction of Fuzzy Emerging Patterns. Cognitive Computation, 1-18.*

## Compile

The algorithm is compiled by means of __sbt__. Assuming sbt is installed in the system, run:

```
sbt clean
sbt compile
sbt package
```

## Data format

Data must follows the KEEL dataset format (http://keel.es/).

## Parameters

The algorithm uses several parameters for its execution. All of them must be provided by means of a parameters file. An example of a fully functional parameters file is provided in this repository. Additional information of paramters will be added soon.

## Execution
For the execution of the algorithm you must call the "spark-submit" command. The simplest way to execute the algorithm in a local environment is:

```
spark-submit BD-EFEP.jar param.txt
```

This will execute the algorithm with the parameters and files presented in the parameters file "param.txt"
