import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class WordCount {

    /**
     * MapReduce程序需要继承 org.apache.hadoop.mapreduce.Mapper 这个类，
     * 并在这个类的继承类中至少自定义实现 Map() 方法，
     * 其中 org.apache.hadoop.mapreduce.Mapper 要求的参数有四个（keyIn、valueIn、keyOut、valueOut），
     * 即Map（）任务的输入和输出都是< key，value >对的形式。
     *
     * 源代码中此处各个参数意义是：
     * 1、Object：输入< key, value >对的 key 值，此处为文本数据的起始位置的偏移量。
     * 在大部分程序下这个参数可以直接使用 Long 类型，源码此处使用Object做了泛化。
     * 2、Text：输入< key, value >对的 value 值，此处为一段具体的文本数据。
     * 3、Text：输出< key, value >对的 key 值，此处为一个单词。
     * 4、IntWritable：输出< key, value >对的 value 值，此处固定为 1 。
     * IntWritable 是 Hadoop 对 Integer 的进一步封装，使其可以进行序列化。
     */

    public static class Map extends
            Mapper<Object, Text, Text, IntWritable> {
        /**
         * 此处定义了两个变量：
         * one：类型为Hadoop定义的 IntWritable 类型，其本质就是序列化的 Integer ，one 变量的值恒为 1
         * word：因为在WordCount程序中，Map 端的任务是对输入数据按照单词进行切分，每个单词为 Text 类型。
         */
        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        /**
         * 这段代码为Map端的核心，定义了Map Task 所需要执行的任务的具体逻辑实现。
         * map() 方法的参数为 Object key, Text value, Context context，其中：
         * @param key 输入数据在原数据中的偏移量
         * @param value 具体的数据数据，此处为一段字符串
         * @param context  用于暂时存储 map() 处理后的结果
         * @throws IOException
         * @throws InterruptedException
         * 方法内部首先把输入值转化为字符串类型，并且对Hadoop自带的分词器 StringTokenizer 进行实例化用于存储输入数据。
         * 之后对输入数据从头开始进行切分，把字符串中的每个单词切分成< key, value >对的形式，
         * 如：< hello , 1>、< world, 1> …
         */
        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {
            String line = value.toString();
            StringTokenizer tokenizer = new StringTokenizer(line);
            while (tokenizer.hasMoreTokens()) {
                word.set(tokenizer.nextToken());
                context.write(word, one);
            }
        }
    }

    /**
     * import org.apache.hadoop.mapreduce.Reducer 类的参数也是四个（keyIn、valueIn、keyOut、valueOut），
     * 即Reduce（）任务的输入和输出都是< key，value >对的形式。
     * 源代码中此处各个参数意义是：
     * 1、Text：输入< key, value >对的key值，此处为一个单词
     * 2、IntWritable：输入< key, value >对的value值。
     * 3、Text：输出< key, value >对的key值，此处为一个单词
     * 4、IntWritable：输出< key, value >对，此处为相同单词词频累加之后的值。实际上就是一个数字。
     */
    public static class Reduce extends
            Reducer<Text, IntWritable, Text, IntWritable> {

        /**
         * Reduce() 的三个参数为：
         * @param key  输入< key, value >对的key值，也就是一个单词
         * @param values  这个地方值得注意，在前面说到了，在MapReduce任务中，除了我们自定义的map()和reduce()之外，
         *                在从map到reduce的过程中，系统会自动进行combine、shuffle、sort等过程对map task的输出进行处理，
         *                因此reduce端的输入数据已经不仅仅是简单的< key, value >对的形式，而是一个一系列key值相同的序列化结构，
         *                如：< hello，1，1，2，2，3…>。因此，此处value的值就是单词后面出现的序列化的结构：（1，1，1，2，2，3…….）
         * @param context 临时存储reduce端产生的结果
         * @throws IOException
         * @throws InterruptedException
         * 因此在reduce端的代码中，对value中的值进行累加，所得到的结果就是对应key值的单词在文本出所出现的词频
         */
        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            context.write(key, new IntWritable(sum));
        }
    }

    /**
     * 主函数
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        // 获取我们在执行这个任务时传入的参数，如输入数据所在路径、输出文件的路径的等


        Job job = new Job(conf, "wordcount"); // 实例化job，传入参数，job的名字叫 word count
        job.setJarByClass(WordCount.class);  //使用反射机制，加载程序

        job.setOutputKeyClass(Text.class);  //设置程序的输出的key值的类型
        job.setOutputValueClass(IntWritable.class);  //设置程序的输出的value值的类型

        job.setMapperClass(Map.class);  //设置job的map阶段的执行类
        job.setReducerClass(Reduce.class);  //设置job的reduce阶段的执行类

        job.setInputFormatClass(TextInputFormat.class); //格式化输入
        job.setOutputFormatClass(TextOutputFormat.class); //格式化输出

        FileInputFormat.addInputPath(job, new Path(args[0]));  //获取我们给定的参数中，输入文件所在路径
        FileOutputFormat.setOutputPath(job, new Path(args[1])); //获取我们给定的参数中，输出文件所在路径

        job.waitForCompletion(true); //等待任务完成
    }

}