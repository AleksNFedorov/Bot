package com.bot.worker.config;

import com.bot.common.TaskConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by Aleks on 11/18/16.
 */
@XmlRootElement(name = "config")
class XmlConfig {

  @XmlElement(name = "group", required = false)
  private List<XmlTaskGroupConfig> groups = new ArrayList<>();

  @XmlElement(name = "task", required = false)
  private List<XmlTaskConfig> tasks = new ArrayList<>();

  List<XmlTaskGroupConfig> getGroups() {
    return groups;
  }

  List<XmlTaskConfig> getTasks() {
    return tasks;
  }

  static class XmlTaskGroupConfig {

    @XmlAttribute(name = "id", required = true)
    private String groupName;

    @XmlElement(name = "task", required = true)
    private List<XmlTaskConfig> tasks;

    String getGroupName() {
      return groupName;
    }

    List<XmlTaskConfig> getTasks() {
      return tasks;
    }

    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this, false);
    }

    @Override
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj);
    }

  }

  @XmlAccessorType(XmlAccessType.PROPERTY)
  static class XmlTaskConfig extends TaskConfig {

    @XmlAttribute(name = "id", required = true)
    public String getTaskName() {
      return taskName;
    }

    void setTaskName(String taskName) {
      this.taskName = taskName;
    }

    @XmlAttribute(name = "executor", required = true)
    @Override
    public String getExecutorId() {
      return executorId;
    }

    void setExecutorId(String executorId) {
      this.executorId = executorId;
    }

    @XmlElement(name = "run", required = true, defaultValue = "" +
        ONE_TIME_TASK)
    @Override
    public long getRunInterval() {
      return runInterval;
    }

    void setRunInterval(long runInterval) {
      this.runInterval = runInterval;
    }

    @XmlElement(name = "deadline")
    @Override
    public long getDeadline() {
      return deadline;
    }

    void setDeadline(long deadline) {
      this.deadline = deadline;
    }

    @XmlJavaTypeAdapter(MapAdapter.class)
    @XmlElement(name = "executorConfig")
    Map<String, String> getConfig() {
      return config;
    }

    void setConfig(Map<String, String> config) {
      this.config = config;
    }

    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this, false);
    }

    @Override
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj);
    }
  }

  private static class ExecutorConfig {

    @XmlElement(name = "property")
    List<MapElement> mapElements;

    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this, false);
    }

    @Override
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj);
    }

  }

  private static class MapElement {

    @XmlAttribute
    private String key;

    @XmlValue
    private String value;

    MapElement() {
    }

    MapElement(String key, String value) {
      this.key = key;
      this.value = value;
    }

    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this, false);
    }

    @Override
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj);
    }
  }

  private static class MapAdapter extends XmlAdapter<ExecutorConfig,
      Map<String, String>> {

    MapAdapter() {
    }

    public ExecutorConfig marshal(Map<String, String> arg0) {
      throw new UnsupportedOperationException();
    }

    public Map<String, String> unmarshal(ExecutorConfig arg0) throws
        Exception {
      Map<String, String> r = new TreeMap<>();
      for (MapElement mapelement : arg0.mapElements) {
        r.put(mapelement.key, mapelement.value);
      }
      return r;
    }
  }
}
