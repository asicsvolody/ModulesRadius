/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.radius;

import org.apache.spark.sql.SparkSession;
import ru.radius.config.ParquetToHiveTableConfig;
import ru.radius.utils.HiveUtils;
import ru.radius.utils.Utils;

import java.io.File;
import java.util.List;

public class ParquetToHiveTable {

    public static SparkSession spark;

    static{
        String warehouseLocation = new File("./metastore_db").getAbsolutePath();
        spark = SparkSession
                .builder()
                .appName("kyloRadius")
                .config("spark.master", "local")
                .config ("hive.exec.dynamic.partition", "true")
                .config ("hive.exec.dynamic.partition.mode", "nonstrict")
                .config("hive.txn.manager", "org.apache.hadoop.hive.ql.lockmgr.DbTxnManager")
                .config("hive.support.concurrency", "true")
                .config("hive.enforce.bucketing", "true")
                .config("spark.yarn.dist.files", "/usr/local/Cellar/hive/3.1.2/libexec/conf/hive-site.xml")
//                .config("hive.metastore.uris", "jdbc:mysql://localhost:3306/metastore?serverTimezone=UTC&zeroDateTimeBehavior=CONVERT_TO_NULL")
//                .config("javax.jdo.option.ConnectionPassword", "bhbyf.hnftdf")
//                .config("javax.jdo.option.ConnectionUserName","root")
//                .config("spark.sql.warehouse.dir", warehouseLocation)
                .config("spark.hadoop.fs.default.name", "hdfs://localhost:8020")
                .config("spark.hadoop.fs.defaultFS", "hdfs://localhost:8020")
                .config("spark.hadoop.fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName())
                .config("spark.hadoop.fs.hdfs.server", org.apache.hadoop.hdfs.server.namenode.NameNode.class.getName())
                .config("spark.hadoop.conf", org.apache.hadoop.hdfs.HdfsConfiguration.class.getName())
                .enableHiveSupport()
                .getOrCreate();
        spark.sparkContext().conf().setSparkHome("/usr/local/Cellar/apache-spark/2.4.4");
        spark.sparkContext().setLogLevel("WARN");

    }

    private ParquetToHiveTableConfig config;



    public ParquetToHiveTable(ParquetToHiveTableConfig config) {
        this.config = config;
    }

    public void paquetToHiveTable() throws Exception {


        List<String> colsList = Utils.getFormattingColsFromDir(config);

        HiveUtils.createTransactionalHiveTable(colsList, config);

        System.out.println("Dynamic insert into hiveTable");

        HiveUtils.insetToHiveTable(config);


        System.out.println("Check data");

        spark.sql(String.format("SELECT * FROM %s.%s",config.getBaseTo(), config.getTableTo()))
                .sort("user_id").show();

//        HDFSUtils.deleteDir(new Path(pathFrom));

    }

    /// dirFrom baseName tableName partitions (ex: user_id/int)

    public static void main(String[] args) throws Exception {
        new ParquetToHiveTable(ParquetToHiveTableConfig.getConfig(args))
                .paquetToHiveTable();
    }



}
