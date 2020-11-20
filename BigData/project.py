""" 
+---------------------------------------+-----------+
| Nagaraj Bahubali Asundi               | 219203256 |
+---------------------------------------+-----------+
| Sriram Aralappa                       | 219203195 |
+---------------------------------------+-----------+
| Soniya Manchenahalli Gnanendra Prasad | 219203190 |
+---------------------------------------+-----------+
| Adarsh Anand                          | 219203365 |
----------------------------------------+-----------+
| Abinaya Thulsi Chandrasekaran         | 219203032 |
+---------------------------------------+-----------+

Regression for COVID-19 Development in Australia
Dataset used : https://github.com/CSSEGISandData/COVID-19/blob/master/archived_data/archived_time_series/time_series_19-covid-Confirmed_archived_0325.csv

"""

from pyspark import SparkContext
from pyspark.sql.session import SparkSession
from pyspark.sql.functions import col, sum, create_map, explode, lit
from itertools import chain
from pyspark.ml.regression import GeneralizedLinearRegression
from pyspark.ml.linalg import Vectors
from pyspark.ml.feature import VectorAssembler
import pyspark.sql.functions as f 
import matplotlib.pyplot as plt

def preprocess(df):

    # Filter  by Australia and remove unwanted columns including dates (from 1/22/20 to 1/31/20)
    df2 = df.filter(col("Country/Region") == "Australia").drop("Province/State").drop("Lat").drop("Long")
    df3 = df2.drop(*df2.columns[1:11]) 

    # Handle null values 
    df4 = df3.na.fill(0)

    # Merge cells by Country
    agg_data = [f.sum(c) for c in df4.columns][1:]
    df5 = df4.groupBy('country/region').agg(*agg_data).drop('country/region')

    return df5

def transform(df):
    # Change date to numbers
    df2 = df.select([col(c).alias(str(i)) for i,c in enumerate(df.columns,0)])

    # Transpose 
    df3 = df2.select(explode(create_map(*chain.from_iterable([(lit(c), col(c)) for c in df2.columns]))))

    # Change the datatypes 
    df4 = df3.withColumn("key",col("key").cast("integer")).withColumn("value",col("value").cast("integer"))

    # Rename the column names for convenience
    df5 = df4.withColumnRenamed("key","Days").withColumnRenamed("value","Case_count")

    return df5

def plot_data(df):

    Days = [int(row['Days']) for row in df.collect()]
    Case_count = [int(row['Case_count']) for row in df.collect()]

    plt.plot(Days, Case_count)
    plt.title('Corona infection counts over days')
    plt.xlabel('Days')
    plt.ylabel('Cases count')
    plt.show()

def build_dataset(df):

    # Create features' vector
    features  =  VectorAssembler(inputCols = ["Days"],outputCol = "Days_vec")
    output = features.transform(df)

    train_data = output.where(col("Days") < 50).select("Days_vec","Case_count")
    test_data = output.where(col("Days") >= 50).select("Days_vec","Case_count")

    return train_data,test_data

def regression(train_set,test_set,featuresColumn,labelColumn):

    regressor = GeneralizedLinearRegression(featuresCol = featuresColumn , labelCol = labelColumn ,family="gaussian", link="log", maxIter=10, regParam=0.3)
    regressor = regressor.fit(train_set)

    predict_results = regressor.evaluate(test_set)
    result = predict_results.predictions

    return result


sc = SparkContext("local","Big Data Assignment")
sc.setLogLevel("ERROR")
spark = SparkSession(sc)

# Import data to spark
data = spark.read.option("inferSchema","true").option("header","true").csv("time_series_19-covid-Confirmed_archived_0325.csv")

# Preprocess data
processed_data = preprocess(data)

# Tranform data
trans_data = transform(processed_data)
# trans_data.show(100)

# # Plot the data to observe the relation
# plot_data(trans_data)

# Create training and testing data
train_data,test_data = build_dataset(trans_data)

# Perform smoothing to avoid zero values
smoothed_train_data= train_data.withColumn('Case_count',train_data['Case_count']+1)
# smoothed_train_data.show(100)

# Perform Regression
result = regression(smoothed_train_data,test_data,featuresColumn = "Days_vec", labelColumn = "Case_count")

# Compute the difference b/w actual and predicted data
result.withColumn('Difference', result['prediction'] - result['Case_count']).show()

sc.stop()