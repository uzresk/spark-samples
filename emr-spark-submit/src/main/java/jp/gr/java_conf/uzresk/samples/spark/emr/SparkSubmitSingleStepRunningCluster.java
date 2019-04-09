package jp.gr.java_conf.uzresk.samples.spark.emr;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder;
import com.amazonaws.services.elasticmapreduce.model.*;
import jp.gr.java_conf.uzresk.samples.spark.emr.utils.AmazonClientConfigurationBuilder;
import jp.gr.java_conf.uzresk.samples.spark.emr.utils.Resource;

import java.util.ArrayList;
import java.util.List;

public class SparkSubmitSingleStepRunningCluster {

    private static final String CLUSTER_ID = "j-1VOWW7554GD9Z";

    public static void main(String[] args) {

        new SparkSubmitSingleStepRunningCluster().startJob();
    }

    private void startJob() {
        ClientConfiguration cc = AmazonClientConfigurationBuilder.clientConfiguration().orElse(new ClientConfiguration());
        AmazonElasticMapReduce emr = AmazonElasticMapReduceClientBuilder.standard()
                .withClientConfiguration(cc)
                .withRegion(Regions.AP_NORTHEAST_1)
                .build();

        List<StepConfig> stepConfigs = new ArrayList<>();
        stepConfigs.add(createCsv2ParquetConfig());

        AddJobFlowStepsRequest req = new AddJobFlowStepsRequest(CLUSTER_ID, stepConfigs);
        AddJobFlowStepsResult result = emr.addJobFlowSteps(req);

        System.out.println(result.getStepIds());
    }

    private StepConfig createCsv2ParquetConfig() {
        HadoopJarStepConfig sparkStepConf = new HadoopJarStepConfig()
                .withJar("command-runner.jar")
                .withArgs("spark-submit",
                        "--executor-memory", "1g",
                        "--deploy-mode", "cluster",
                        "--class", Resource.getString("job.class"),
                        Resource.getString("job.jar"),
                        Resource.getString("csv2parquet.csv-file"),
                        Resource.getString("csv2parquet.parquet-path"));
        return new StepConfig()
                .withName("Csv2Parquet")
                .withActionOnFailure(ActionOnFailure.TERMINATE_CLUSTER)
                .withHadoopJarStep(sparkStepConf);
    }
}
