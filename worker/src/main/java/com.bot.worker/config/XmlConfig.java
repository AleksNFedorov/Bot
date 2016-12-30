package com.bot.worker.config;

import com.bot.common.TaskConfig;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

        @XmlAttribute(name = "key", required = true)
        private String groupName;

        @XmlElement(name = "task", required = true)
        private List<XmlTaskConfig> tasks;

        String getGroupName() {
            return groupName;
        }

        List<XmlTaskConfig> getTasks() {
            return tasks;
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
    }

    private static class ExecutorConfig {
        @XmlElement(name = "property")
        List<MapElement> mapElements;
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
