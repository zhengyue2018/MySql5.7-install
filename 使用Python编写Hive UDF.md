Hive 内置为我们提供了大量的常用函数用于日常的分析，但是总有些情况这些函数还是无法满足我们的需求；值得高兴的是，Hive 允许用户自定义一些函数，用于扩展 HiveQL 的功能，这类函数叫做 UDF（用户自定义函数）。使用 Java 编写 UDF 是最常见的方法，但是本文介绍的是如何使用 Python 来编写 Hive 的 UDF 函数

假设我们有个名为 data.txt 的文件，我放在/opt下边格式如下：

```txt
RAVI kumar
Anish kumar
Rakesh jha
Vishal kumar
Ananya ghosh
```
上面文件的内容每一行代表一个人的名字，现在我们需要使用 Hive 分别获取到每个人的 First name 和 Last name。我们 Hive 表的建表语句如下：
```sql
CREATE TABLE `mytable`(
  `fname` string, 
  `lname` string
)
```
我们现在将上面的数据导入到这个表中：
```sh
load data local inpath './opt/data.txt' into table mytable;
```
我们直接 select 出来的数据如下：
```sh
hive> select * from mytable;
OK
RAVI kumar	NULL
Anish kumar	NULL
Rakesh jha	NULL
Vishal kumar	NULL
Ananya ghosh	NULL
Time taken: 2.341 seconds, Fetched: 5 row(s)
```
这不是我们要的数据，因为每一行的数据全部解析到 fname 字段，而 lname 字段并没有值，所以最后一列为 NULL。现在我们编写一个 Python 脚本来处理这个问题：
```py
#!/usr/bin/python
 
import sys
 
for line in sys.stdin:
        line = line.strip()
        fname , lname = line.split(' ')
        l_name = lname.lower()
        print '\t'.join([fname, str(l_name)])
```
上面的脚本意思是将每行的数据按照空格分割，然后分别赋值给 fname 和 lname。下面我们到 Hive 中使用 Python 编写好的 UDF，语法如下：
```sql
SELECT TRANSFORM(stuff)
USING 'script'
AS thing1, thing2
 
or
 
SELECT TRANSFORM(stuff)
USING 'script'
AS (thing1 INT, thing2 INT)
```
通过 Python 解析好的数据全部都是 String 类型的，如果你需要转换成其他类型，可以使用第二个语法。所有我们的例子里面可以如下使用：
```sql
hive> add file /opt/iteblog.py
    > ;
Added resources: [/opt/iteblog.py]
hive> select TRANSFORM (fname) USING "python iteblog.py" as (fname,lname) from mytable;
WARNING: Hive-on-MR is deprecated in Hive 2 and may not be available in the future versions. Consider using a different execution engine (i.e. spark, tez) or using Hive 1.X releases.
Query ID = root_20190215150250_24037329-4975-43dc-9476-de8818652cdb
Total jobs = 1
Launching Job 1 out of 1
Number of reduce tasks is set to 0 since there's no reduce operator
Starting Job = job_1548295046270_0010, Tracking URL = http://localhost:8088/proxy/application_1548295046270_0010/
Kill Command = /opt/hadoop/hadoop-3.0.3/bin/hadoop job  -kill job_1548295046270_0010
Hadoop job information for Stage-1: number of mappers: 1; number of reducers: 0
2019-02-15 15:03:17,301 Stage-1 map = 0%,  reduce = 0%
2019-02-15 15:03:37,096 Stage-1 map = 100%,  reduce = 0%, Cumulative CPU 1.41 sec
MapReduce Total cumulative CPU time: 1 seconds 410 msec
Ended Job = job_1548295046270_0010
MapReduce Jobs Launched: 
Stage-Stage-1: Map: 1   Cumulative CPU: 1.41 sec   HDFS Read: 4318 HDFS Write: 207 SUCCESS
Total MapReduce CPU Time Spent: 1 seconds 410 msec
OK
RAVI	kumar
Anish	kumar
Rakesh	jha
Vishal	kumar
Ananya	ghosh
Time taken: 50.424 seconds, Fetched: 5 row(s)
```
正如上面的结果，我们已经获取到需要的结果。
