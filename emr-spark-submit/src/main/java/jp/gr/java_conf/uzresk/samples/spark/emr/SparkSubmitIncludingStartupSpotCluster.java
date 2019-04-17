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

import static com.amazonaws.services.elasticmapreduce.model.InstanceRoleType.CORE;
import static com.amazonaws.services.elasticmapreduce.model.InstanceRoleType.MASTER;

public class SparkSubmitIncludingStartupSpotCluster {

    public static void main(String[] args) {
        ClientConfiguration cc = AmazonClientConfigurationBuilder.clientConfiguration().orElse(new ClientConfiguration());
        AmazonElasticMapReduce emr = AmazonElasticMapReduceClientBuilder.standard()
                .withClientConfiguration(cc)
                .withRegion(Regions.AP_NORTHEAST_1)
                .build();
        new SparkSubmitIncludingStartupSpotCluster().launch(emr);
    }

    private void launch(AmazonElasticMapReduce emr) {

        RunJobFlowRequest request = createRequest();

        RunJobFlowResult result = emr.runJobFlow(request);

        System.out.println(result.getJobFlowId() + " is starting");

    }

    private RunJobFlowRequest createRequest() {

        // withKeepJobFlowAliveWHenNoStepsは起動後にStepを登録していなくてもClusterを残し続けるオプション
        // withVisibleToAllUsersは、起動するときに使用したIAMユーザー以外のユーザーもこのクラスターを表示・管理させるオプション
        // withVisibleToAllUsersは絶対に外さないで下さい。もし外してしまうとAWSコンソールや他のCLI、Lambda上から作成したClusterを操作することができなくなります。
        return new RunJobFlowRequest()
                .withName(Resource.getString("emr.cluster-name"))
                .withReleaseLabel(Resource.getString("emr.release-label"))
                .withSteps(createStepConfig())
                .withApplications(createApplicationList())
                .withTags(new Tag("Name", Resource.getString("emr.cluster-name")))
                .withEbsRootVolumeSize(Resource.getInt("emr.ebs-root-volume-size"))
                .withServiceRole(Resource.getString("emr.service-role"))
                .withAutoScalingRole(Resource.getString("emr.auto-scaling-role"))
                .withJobFlowRole(Resource.getString("emr.job-flow-role"))
                .withLogUri(Resource.getString("emr.log-uri"))
                .withVisibleToAllUsers(true)
                .withInstances(createJobFlowInstancesConfig(createMasterConfig(), createCoreConfig()));
    }

    private List<StepConfig> createStepConfig() {
        List<StepConfig> configs = new ArrayList<>();
        configs.add(createDebugConfig());
        configs.add(createCsv2ParquetConfig());
        return configs;
    }

    private StepConfig createDebugConfig() {
        String COMMAND_RUNNER = "command-runner.jar";
        String DEBUGGING_COMMAND = "state-pusher-script";
        String DEBUGGING_NAME = "Setup Hadoop Debugging";

        return new StepConfig()
                .withName(DEBUGGING_NAME)
                .withActionOnFailure(ActionOnFailure.TERMINATE_CLUSTER)
                .withHadoopJarStep(
                        new HadoopJarStepConfig().withJar(COMMAND_RUNNER).withArgs(DEBUGGING_COMMAND));
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

    private ArrayList<Application> createApplicationList() {
        ArrayList<Application> applications = new ArrayList<>();
        applications.add(new Application().withName("Spark"));
        return applications;
    }

    private InstanceGroupConfig createMasterConfig() {
        return new InstanceGroupConfig()
                .withBidPrice("0.10")          // $0.10
                .withMarket(MarketType.SPOT)   // Spotインスタンスを利用する
                .withInstanceCount(Resource.getInt("emr.master.instance-count"))
                .withEbsConfiguration(
                        new EbsConfiguration()
                                .withEbsBlockDeviceConfigs(
                                        new EbsBlockDeviceConfig()
                                                .withVolumeSpecification(
                                                        new VolumeSpecification().withSizeInGB(Resource.getInt("emr.master.volume-size")).withVolumeType(Resource.getString("emr.master.volume-type")))
                                                .withVolumesPerInstance(1))
                                .withEbsOptimized(true))
                .withInstanceRole(MASTER)
                .withInstanceType(Resource.getString("emr.master.instance-type"))
                .withName(Resource.getString("emr.master.name"));
    }

    private InstanceGroupConfig createCoreConfig() {
        return new InstanceGroupConfig()
                .withBidPrice("0.10")          // $0.10
                .withMarket(MarketType.SPOT)   // Spotインスタンスを利用する
                .withInstanceCount(Resource.getInt("emr.core.instance-count"))
                .withEbsConfiguration(
                        new EbsConfiguration()
                                .withEbsBlockDeviceConfigs(
                                        new EbsBlockDeviceConfig()
                                                .withVolumeSpecification(
                                                        new VolumeSpecification().withSizeInGB(Resource.getInt("emr.core.volume-size")).withVolumeType(Resource.getString("emr.core.volume-type")))
                                                .withVolumesPerInstance(1))
                                .withEbsOptimized(true))
                .withInstanceRole(CORE)
                .withInstanceType(Resource.getString("emr.core.instance-type"))
                .withName(Resource.getString("emr.core.name"));
    }

    private JobFlowInstancesConfig createJobFlowInstancesConfig(
            InstanceGroupConfig masterConfiguration, InstanceGroupConfig coreConfiguration) {
        return new JobFlowInstancesConfig()
                .withInstanceGroups(masterConfiguration, coreConfiguration)
                .withEc2SubnetId(Resource.getString("emr.subnet-id"))
                .withEmrManagedMasterSecurityGroup(Resource.getString("emr.master-sg"))
                .withEmrManagedSlaveSecurityGroup(Resource.getString("emr.slave-sg"))
                .withEc2KeyName(Resource.getString("emr.key-name"))
                .withKeepJobFlowAliveWhenNoSteps(false); // JOB終了時にインスタンスを停止する
    }
}
