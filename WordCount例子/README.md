# WordCount小程序练习
## 0.前提
首先要安装好Hadoop，并对Hadoop进行配置，这里就不再多说了，大家可以参考Hadoop相关的配置文档。    
重点提一下mapred-site.xml的配置，因为这个不配置好运行MapReduce时候会报错    
配置文件可以参考如下：
```sh
<configuration>
<!-- 通知框架MR使用YARN -->
    <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
    </property>

    <property>  
     <name>mapreduce.application.classpath</name>  
      <value>  
    	/opt/hadoop/hadoop-3.0.3/etc/hadoop,  
    	/opt/hadoop/hadoop-3.0.3/share/hadoop/common/*,  
    	/opt/hadoop/hadoop-3.0.3/share/hadoop/common/lib/*,  
    	/opt/hadoop/hadoop-3.0.3/share/hadoop/hdfs/*,  
    	/opt/hadoop/hadoop-3.0.3/share/hadoop/hdfs/lib/*,  
    	/opt/hadoop/hadoop-3.0.3/share/hadoop/mapreduce/*,  
    	/opt/hadoop/hadoop-3.0.3/share/hadoop/mapreduce/lib/*,  
    	/opt/hadoop/hadoop-3.0.3/share/hadoop/yarn/*,  
        /opt/hadoop/hadoop-3.0.3/share/hadoop/yarn/lib/*  
       </value>  
    </property>
</configuration>
```

配置完成之后，我们我们还需要什么？

1.需要在HDFS中保存有文件。

2.需要一个程序jar包，我们前面说过，JobTracker接收jar包就会分解job为mapTask和reduceTask。mapTask会读取HDFS中的文件来执行。
## 1.文件存入HDFS
我们输入两个文件，file1和file2。交给hadoop执行之后，会返回file1和file2文件中的单词的计数。

我们说过，hadoop返回的是<key，value>的键值对的形式。

所以类似结果如下：也就是把单词以及单词的个数返回
```sh
hadoop	2
hello	2
love	6
world	2
```
所以我们首先创建两个文件：file1和file2。 
随便填点东西在里面，文件中的内容是用来计数。单词之间用空格分隔，当然这是不一定的，如何区分单词是在后面jar包中的map程序中分辨的。

我们写好了这两个文件之后，要将文件提交到HDFS中。如何提交呢？    
提交之前，首先要确保hadoop已经运行起来了，查看jps可以看到hadoop的进程。     
首先我们在hadoop的HDFS中创建一个文件夹。

```sh
hdfs dfs -mkdir /input_wordcount
```
这样就可以在HDFS根目录下创建一个input_wordcount的文件夹 

提交我们创建的两个文件
```sh
hdfs dfs -put input/* /input_wordcount
```
查看下
```
hdfs dfs -ls /input_wordcount
```
第一个要求完成了，接下来我们就需要一个程序jar包。
## 2.程序jar包
源代码参考[wordcount程序](./mapreduce)

代码的解释也在里边，mvn package后会生成jar包，得到<font color='aa0000'>wordcount-1.0-SNAPSHOT.jar</font>就完成这一步的任务了

## 3. 作业提交给Hadoop执行
```sh
hadoop jar wordcount-1.0-SNAPSHOT.jar WordCount /input_wordcount /output_word
```
hadoop jar wordcount-1.0-SNAPSHOT.jar WordCount这里提交jar包，告诉主类在哪，后边两个就是输入输出参数了，执行完成可以看到：
```sh
[root@localhost opt]# hdfs dfs -ls /output_word
Found 2 items
-rw-r--r--   1 root supergroup          0 2019-01-31 14:32 /output_word/_SUCCESS
-rw-r--r--   1 root supergroup         32 2019-01-31 14:32 /output_word/part-r-00000
```
在part-r-00000就是运行结果了：
```sh
[root@localhost opt]# hdfs dfs -cat /output_word/part-r-00000
hadoop	2
hello	2
love	6
world	2
```
得到结果了吧。

对于hadoop来说，执行任务需要操作HDFS，需要job对应的jar包。而jar包中需要编写mapTask和ReduceTask对应的方法。交给jobTracker执行就可以了。