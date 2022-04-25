package ir.ac.sbu.graph.producer.config;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@EnableConfigurationProperties
public class ApplicationConfigs {

    private String anchorsParquetDirectory;
    private String keywordsParquetDirectory;

    private final HadoopConfigs hadoopConfigs = new HadoopConfigs();
    private final SparkConfigs sparkConfigs = new SparkConfigs();

    public String getAnchorsParquetDirectory() {
        return anchorsParquetDirectory;
    }

    public void setAnchorsParquetDirectory(String anchorsParquetDirectory) {
        this.anchorsParquetDirectory = anchorsParquetDirectory;
    }

    public String getKeywordsParquetDirectory() {
        return keywordsParquetDirectory;
    }

    public void setKeywordsParquetDirectory(String keywordsParquetDirectory) {
        this.keywordsParquetDirectory = keywordsParquetDirectory;
    }

    public HadoopConfigs getHadoopConfigs() {
        return hadoopConfigs;
    }

    public SparkConfigs getSparkConfigs() {
        return sparkConfigs;
    }

    public static class HadoopConfigs {

        private String hadoopNameNodeHostname;
        private int hadoopNameNodePort;

        public String getHadoopNameNodeHostname() {
            return hadoopNameNodeHostname;
        }

        public void setHadoopNameNodeHostname(String hadoopNameNodeHostname) {
            this.hadoopNameNodeHostname = hadoopNameNodeHostname;
        }

        public int getHadoopNameNodePort() {
            return hadoopNameNodePort;
        }

        public void setHadoopNameNodePort(int hadoopNameNodePort) {
            this.hadoopNameNodePort = hadoopNameNodePort;
        }
    }

    public static class SparkConfigs {

        private String sparkMasterAddress;
        private Map<String, String> sparkConfigs;

        public String getSparkMasterAddress() {
            return sparkMasterAddress;
        }

        public void setSparkMasterAddress(String sparkMasterAddress) {
            this.sparkMasterAddress = sparkMasterAddress;
        }

        public Map<String, String> getSparkConfigs() {
            return sparkConfigs;
        }

        public void setSparkConfigs(Map<String, String> sparkConfigs) {
            this.sparkConfigs = sparkConfigs;
        }
    }
}
